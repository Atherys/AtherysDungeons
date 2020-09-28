package com.atherys.dungeons.service;

import com.atherys.dto.RegisterServerDTO;
import com.atherys.dto.UnregisterServerDTO;
import com.atherys.dungeons.AtherysDungeons;
import com.atherys.dungeons.AtherysDungeonsConfig;
import com.atherys.dungeons.model.Dungeon;
import com.atherys.dungeons.model.DungeonInstance;
import com.atherys.dungeons.service.exception.DungeonInstantiationException;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A class responsible for creating new dungeon minecraft server instances.
 */
@Singleton
public class DungeonInstantiationService {

    @Inject
    Logger logger;

    @Inject
    private AtherysDungeonsConfig config;

    @Inject
    private PluginMessageService pluginMessageService;

    private Random rand = new Random();

    private Map<String, List<DungeonInstance>> dungeonInstances = new HashMap<>();

    private PteroApplication pteroApplication;

    private PteroClient pteroClient;

    private AtomicInteger numberOfAvailableInstances = new AtomicInteger(0);

    private Task worker;

    public DungeonInstantiationService() {
    }

    public void init() {
        if (worker != null) {
            worker.cancel();
        }

        pteroApplication = new PteroBuilder()
                .setApplicationUrl(config.PTERODACTYL_CONFIG.APPLICATION_URL)
                .setToken(config.PTERODACTYL_CONFIG.APPLICATION_TOKEN)
                .build()
                .asApplication();

        pteroClient = new PteroBuilder()
                .setApplicationUrl(config.PTERODACTYL_CONFIG.APPLICATION_URL)
                .setToken(config.PTERODACTYL_CONFIG.CLIENT_TOKEN)
                .build()
                .asClient();

        numberOfAvailableInstances = new AtomicInteger(config.PTERODACTYL_CONFIG.PORTS.size());

        // create a number of allocations based on the size of the listed available ports
        pteroApplication.retrieveNodeById(config.PTERODACTYL_CONFIG.NODE_ID).executeAsync(
                node -> config.PTERODACTYL_CONFIG.PORTS.forEach(port -> node.getAllocationManager().createAllocation()
                        .setIP(config.PTERODACTYL_CONFIG.ALLOCATIONS_IP_ADDRESS)
                        .setPorts(port)
                        .setAlias("localhost")
                        .build()
                        .executeAsync(v -> {}, ex -> logger.error("Failed to create allocation for port " + port, ex.toString()))),
                fail -> logger.error("Could not retrieve Node with id " + config.PTERODACTYL_CONFIG.NODE_ID, fail));

        worker = Sponge.getScheduler().createTaskBuilder()
                .async()
                .interval(5, TimeUnit.SECONDS)
                .execute(this::work)
                .submit(AtherysDungeons.getInstance());
    }

    // clear all currently running instances, regardless of state
    public void stop() {
        Thread thread = new Thread(() -> {
            dungeonInstances.values().parallelStream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
                    .forEach(this::shutdownAndDeleteInstance);
        });

        // hang the server process until all servers are shut down and deleted
        thread.setDaemon(false);
        thread.start();
    }

    public void createDungeonInstance(Dungeon dungeon, Consumer<DungeonInstance> success, Consumer<DungeonInstantiationException> failure) {
        if (numberOfAvailableInstances.get() <= 0) {
            return;
        }

        // decrement early to prevent any kind of async weirdness
        numberOfAvailableInstances.decrementAndGet();

        String serverName = StringUtils.replace(dungeon.getName() + "-" + calcDungeonInstanceSequenceNumber(dungeon), " ", "-");

        pteroApplication.retrieveNodeById(config.PTERODACTYL_CONFIG.NODE_ID)
                .executeAsync(node -> {
                    Optional<Allocation> freeAllocation = pteroApplication.retrieveAllocationsByNode(node)
                            .execute()
                            .stream()
                            .filter(allocation -> config.PTERODACTYL_CONFIG.PORTS.contains(allocation.getPort()) && !allocation.isAssigned())
                            .findAny();

                    if (!freeAllocation.isPresent()) {
                        failToCreateServerInstance(failure, "No available allocations", null);
                        return;
                    }

                    User user = pteroApplication.retrieveUserById(config.PTERODACTYL_CONFIG.USER_ID).execute();

                    if (user == null) {
                        failToCreateServerInstance(failure, "Invalid User ID in configuration", null);
                        return;
                    }

                    Nest nest = pteroApplication.retrieveNestById(dungeon.getNestId()).execute();
                    Egg egg = pteroApplication.retrieveEggById(nest, dungeon.getEggId()).execute();

                    Set<Location> locations = pteroApplication.retrieveLocations()
                            .execute()
                            .stream()
                            .filter(location -> config.PTERODACTYL_CONFIG.LOCATION_IDS.contains(location.getIdLong()))
                            .collect(Collectors.toSet());

                    if (locations.isEmpty()) {
                        failToCreateServerInstance(failure, "None of the configured server locations could be found", null);
                        return;
                    }

                    pteroApplication.createServer()
                            .setStartupCommand(dungeon.getInstanceSettings().getStartupCommand()) // required
                            .setOwner(user) // required
                            .setName(serverName) // required
                            .setPack(dungeon.getPackId()) // required
                            .setAllocations(1L) // required
                            .setPortRange(Collections.singleton(freeAllocation.get().getPort()))
                            .startOnCompletion(true) // required
                            .setLocations(locations)
                            .setEgg(egg)
                            .setMemory(dungeon.getInstanceSettings().getMemory(), DataType.MB)
                            .setCPU(dungeon.getInstanceSettings().getCpu())
                            .setDisk(dungeon.getInstanceSettings().getDisk(), DataType.MB)
                            .setSwap(dungeon.getInstanceSettings().getSwap(), DataType.MB)
                            .setEnvironment(dungeon.getInstanceSettings().getEnvironment())
                            .build()
                            .executeAsync(applicationServer -> {
                                DungeonInstance instance = new DungeonInstance();
                                instance.setName(serverName);
                                instance.setDungeon(dungeon);
                                instance.setApplicationServer(applicationServer);
                                instance.setAllocation(freeAllocation.get());
                                instance.setRegistered(false);
                                instance.setOnRegistration(success);
                                //registerDungeonInstance(instance);

                                List<DungeonInstance> instances = dungeonInstances.computeIfAbsent(instance.getDungeon().getName(), k -> new ArrayList<>());
                                instances.add(instance);
                            }, (fail) -> {
                                failToCreateServerInstance(failure, "", fail);
                            });
                });
    }

    private void failToCreateServerInstance(Consumer<DungeonInstantiationException> failure, String message, Throwable exception) {
        failure.accept(new DungeonInstantiationException(message));

        if (exception == null) {
            logger.error("Failed to create server instance: " + message);
        } else {
            logger.error("Failed to create server instance: " + message, exception);
        }

        numberOfAvailableInstances.incrementAndGet();
    }

    public int fetchNumberOfAvailableInstances() {
        return numberOfAvailableInstances.get();
    }

    private int calcDungeonInstanceSequenceNumber(Dungeon dungeon) {
        return dungeonInstances.getOrDefault(dungeon.getName(), new ArrayList<>()).size();
    }

    private void registerDungeonInstance(DungeonInstance instance) {
        RegisterServerDTO dto = new RegisterServerDTO();
        dto.setPort((int) instance.getAllocation().getPortLong());
        dto.setIpAddress(instance.getAllocation().getIP());
        dto.setKey(instance.getName());
        dto.setMotd("Dungeon Instance");
        dto.setName(instance.getName());

        logger.info("Sending proxy registration request...");
        // register server with proxy
        pluginMessageService.proxyRequestServerRegistration(dto);

        logger.info("Calling onRegistration handler");
        // call success callback
        instance.getOnRegistration().accept(instance);

        logger.info("Flagging server as registered");
        instance.setRegistered(true);
    }

    /**
     * Iterate through all currently running instances.
     * If any are stopped, delete them and re-increment numberOfAvailableInstances
     */
    private void work() {
        // fetch random dungeon instance and do work on it every tick.
        // The reason we do not do work for all dungeon instances every tick is so we don't overload the host with requests
        // and get ourselves limited by the proxy or flagged by ddos protection
        Optional<DungeonInstance> dungeonInstance = dungeonInstances.values().stream()
                .flatMap(List::stream)
                .findAny();

        // there are no available dungeon instances to do work on
        if (!dungeonInstance.isPresent()) {
            return;
        }

        DungeonInstance instance = dungeonInstance.get();

        // fetch server by identifier
        pteroClient.retrieveServerByIdentifier(instance.getApplicationServer().getIdentifier())
                .executeAsync(server -> {

                    // fetch server utilization
                    Utilization utilization = pteroClient.retrieveUtilization(server).execute();

                    // If the server has not been registered yet, and it is in the ON state, register it
                    if (!instance.isRegistered() && UtilizationState.ON.equals(utilization.getState())) {
                        logger.info("Registering server " + instance.getName());
                        registerDungeonInstance(instance);
                    }

                    // if off, delete server with force
                    if (instance.isRegistered() && UtilizationState.OFF.equals(utilization.getState())) {
                        logger.info("Deleting server " + instance.getName());
                        deleteInstance(instance);
                    }
                });
    }

    private void shutdownAndDeleteInstance(DungeonInstance instance) {
        UtilizationState utilizationState = UtilizationState.ON;

        while (!UtilizationState.OFF.equals(utilizationState)) {
            ClientServer server = pteroClient.retrieveServerByIdentifier(instance.getApplicationServer().getIdentifier()).execute();
            Utilization utilization = pteroClient.retrieveUtilization(server).execute();

            if (!UtilizationState.STOPPING.equals(utilization.getState()) || !UtilizationState.OFF.equals(utilization.getState())) {
                // while the utilization state continues to not be "OFF" or "STOPPING", keep sending "stop" commands
                pteroClient.sendCommand(server, "stop");
            }

            utilizationState = utilization.getState();
        }

        // Once it is OFF, delete the server
        deleteInstance(instance);
    }

    private void deleteInstance(DungeonInstance instance) {
        instance.getApplicationServer()
                .getController()
                .delete(true) // delete forcefully, just to be safe
                .execute();

        UnregisterServerDTO unregisterServerDTO = new UnregisterServerDTO();
        unregisterServerDTO.setKey(instance.getName());

        pluginMessageService.proxyRequestServerUnregistration(unregisterServerDTO);

        instance.setRegistered(false);

        dungeonInstances.remove(instance.getName());

        // Re-increment available number of instances
        numberOfAvailableInstances.incrementAndGet();
    }

}

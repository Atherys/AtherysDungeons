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
import com.mattmalec.pterodactyl4j.application.entities.Allocation;
import com.mattmalec.pterodactyl4j.application.entities.Node;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.application.entities.User;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    private PteroAPI pterodactyl;

    private AtomicInteger numberOfAvailableInstances = new AtomicInteger(0);

    private Task healthChecker;

    public DungeonInstantiationService() {
    }

    public void init() {
        pterodactyl = new PteroBuilder()
                .setApplicationUrl(config.PTERODACTYL_CONFIG.APPLICATION_URL)
                .setToken(config.PTERODACTYL_CONFIG.APPLICATION_API_TOKEN)
                .build();

        numberOfAvailableInstances = new AtomicInteger(config.PTERODACTYL_CONFIG.PORTS.size());

        // create a number of allocations based on the size of the listed available ports
        pterodactyl.asApplication().retrieveNodeById(config.PTERODACTYL_CONFIG.NODE_ID).executeAsync(
                node -> config.PTERODACTYL_CONFIG.PORTS.forEach(port -> node.getAllocationManager().createAllocation()
                        .setIP(config.PTERODACTYL_CONFIG.ALLOCATIONS_IP_ADDRESS)
                        .setPorts(port)
                        .setAlias("localhost")
                        .build()
                        .executeAsync(v -> {}, ex -> logger.error("Failed to create allocation for port " + port + ": " + ex.toString()))),
                fail -> logger.error("Could not retrieve Node with id " + config.PTERODACTYL_CONFIG.NODE_ID + ": " + fail.toString()));

        healthChecker = Sponge.getScheduler().createTaskBuilder()
                .async()
                .interval(1, TimeUnit.SECONDS)
                .execute(this::healthCheckInstances)
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

        PteroApplication pteroApplication = pterodactyl.asApplication();

        // decrement early to prevent any kind of async weirdness
        numberOfAvailableInstances.decrementAndGet();

        String serverName = dungeon.getName() + " " + calcDungeonInstanceSequenceNumber(dungeon);

        pteroApplication.retrieveNodeById(config.PTERODACTYL_CONFIG.NODE_ID)
                .executeAsync(node -> {
                    Optional<Allocation> freeAllocation = pteroApplication.retrieveAllocationsByNode(node)
                            .execute()
                            .stream()
                            .filter(allocation -> config.PTERODACTYL_CONFIG.PORTS.contains(allocation.getPort()) && !allocation.isAssigned())
                            .findAny();

                    if (!freeAllocation.isPresent()) {
                        failure.accept(new DungeonInstantiationException("No available allocations, can't instantiate new instance"));
                        numberOfAvailableInstances.incrementAndGet();
                        return;
                    }

                    User user = pteroApplication.retrieveUserById(config.PTERODACTYL_CONFIG.USER_ID).execute();

                    if (user == null) {
                        throw new DungeonInstantiationException("Invalid User ID in configuration, can't instantiate new instance");
                    }

                    pteroApplication.createServer()
                            .setStartupCommand(dungeon.getInstanceSettings().getStartupCommand()) // required
                            .setOwner(user) // required
                            .setName(serverName) // required
                            .setPack(dungeon.getPackId()) // required
                            .setAllocations(freeAllocation.get().getIdLong()) // required
                            .startOnCompletion(true) // required
                            .setMemory(dungeon.getInstanceSettings().getMemory(), DataType.MB)
                            .setCPU(dungeon.getInstanceSettings().getCpu())
                            .setDisk(dungeon.getInstanceSettings().getDisk(), DataType.MB)
                            .setSwap(dungeon.getInstanceSettings().getSwap(), DataType.MB)
                            .build()
                            .executeAsync(applicationServer -> {
                                DungeonInstance instance = new DungeonInstance();
                                instance.setName(serverName.replace(' ', '-'));
                                instance.setDungeon(dungeon);
                                instance.setApplicationServer(applicationServer);
                                instance.setAllocation(freeAllocation.get());

                                registerDungeonInstance(instance);

                                success.accept(instance);
                            }, (fail) -> {
                                numberOfAvailableInstances.incrementAndGet();
                                logger.error("Failed to create server instance: " + fail.toString());
                            });
                });
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

        pluginMessageService.proxyRequestServerRegistration(dto);

        List<DungeonInstance> instances = dungeonInstances.computeIfAbsent(instance.getDungeon().getName(), k -> new ArrayList<>());
        instances.add(instance);
    }

    /**
     * Iterate through all currently running instances.
     * If any are stopped, delete them and re-increment numberOfAvailableInstances
     */
    private void healthCheckInstances() {
        dungeonInstances.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(instance -> {
                    PteroClient client = pterodactyl.asClient();

                    // fetch server by identifier
                    client.retrieveServerByIdentifier(instance.getApplicationServer().getIdentifier())
                            .executeAsync(server -> {
                                // fetch server utilization
                                Utilization utilization = client.retrieveUtilization(server).execute();

                                // if off, delete server with force
                                if (UtilizationState.OFF.equals(utilization.getState())) {
                                    deleteInstance(instance);
                                }
                            });
                });
    }

    private void shutdownAndDeleteInstance(DungeonInstance instance) {
        PteroClient client = pterodactyl.asClient();

        UtilizationState utilizationState = UtilizationState.ON;

        while (!UtilizationState.OFF.equals(utilizationState)) {
            ClientServer server = client.retrieveServerByIdentifier(instance.getApplicationServer().getIdentifier()).execute();
            Utilization utilization = client.retrieveUtilization(server).execute();

            if (!UtilizationState.STOPPING.equals(utilization.getState()) || !UtilizationState.OFF.equals(utilization.getState())) {
                // while the utilization state continues to not be "OFF" or "STOPPING", keep sending "stop" commands
                client.sendCommand(server, "stop");
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

        // Re-increment available number of instances
        numberOfAvailableInstances.incrementAndGet();
    }

}

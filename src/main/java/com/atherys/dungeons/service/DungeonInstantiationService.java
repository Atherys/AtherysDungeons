package com.atherys.dungeons.service;

import com.atherys.dungeons.AtherysDungeons;
import com.atherys.dungeons.AtherysDungeonsConfig;
import com.atherys.dungeons.model.Dungeon;
import com.atherys.dungeons.model.DungeonInstance;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    private Map<String, List<DungeonInstance>> dungeonInstances = new HashMap<>();

    private PteroAPI pterodactyl;

    private int numberOfAvailableInstances = 0;

    private Task healthChecker;

    public DungeonInstantiationService() {
    }

    public void init() {
        pterodactyl = new PteroBuilder()
                .setApplicationUrl(config.PTERODACTYL_CONFIG.APPLICATION_URL)
                .setToken(config.PTERODACTYL_CONFIG.APPLICATION_API_TOKEN)
                .build();

        numberOfAvailableInstances = config.MAX_NUMBER_OF_INSTANCES;

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

    public void createDungeonInstance(Dungeon dungeon, Consumer<DungeonInstance> instanceConsumer) {
        if (numberOfAvailableInstances <= 0) {
            return;
        }

        // decrement early to prevent any kind of async weirdness
        numberOfAvailableInstances--;

        String serverName = dungeon.getName() + " " + calcDungeonInstanceSequenceNumber(dungeon);

        pterodactyl.asApplication()
                .createServer()
                .setStartupCommand("java -jar") // TODO: Required
                .setOwner(null) // TODO: Required
                .setName(serverName) // required
                .setMemory(dungeon.getInstanceSettings().getMaxMemory(), DataType.MB)
                // TODO: Add other instance settings
                .setPack(dungeon.getPackId())
                .startOnCompletion(true)
                .build()
                .executeAsync(applicationServer -> {
                    DungeonInstance instance = new DungeonInstance();
                    instance.setDungeon(dungeon);
                    instance.setApplicationServer(applicationServer);
                    registerDungeonInstance(instance);
                    instanceConsumer.accept(instance);
                }, (failure) -> {
                    numberOfAvailableInstances++;
                    logger.error("Failed to create server instance: " + failure.toString());
                });
    }

    private int calcDungeonInstanceSequenceNumber(Dungeon dungeon) {
        return dungeonInstances.getOrDefault(dungeon.getName(), new ArrayList<>()).size();
    }

    private void registerDungeonInstance(DungeonInstance instance) {
        List<DungeonInstance> instances = dungeonInstances.computeIfAbsent(instance.getDungeon().getName(), k -> new ArrayList<>());
        instances.add(instance);
    }

    public int fetchNumberOfAvailableInstances() {
        return numberOfAvailableInstances;
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

            if (!UtilizationState.STOPPING.equals(utilization.getState()) || !UtilizationState.OFF.equals(utilization.getState()) ) {
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
                .delete(true)
                .execute();

        // Re-increment available number of instances
        numberOfAvailableInstances++;
    }
}

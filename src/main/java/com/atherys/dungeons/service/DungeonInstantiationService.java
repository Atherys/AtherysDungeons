package com.atherys.dungeons.service;

import com.atherys.dungeons.AtherysDungeonsConfig;
import com.atherys.dungeons.model.Dungeon;
import com.atherys.dungeons.model.DungeonInstance;
import com.atherys.dungeons.model.InstanceSettings;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.application.entities.Egg;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A class responsible for creating new dungeon minecraft server instances.
 */
@Singleton
public class DungeonInstantiationService {

    @Inject
    private AtherysDungeonsConfig config;

    private Map<String, List<DungeonInstance>> dungeonInstances = new HashMap<>();

    private PteroAPI pterodactyl;

    public DungeonInstantiationService() {
    }

    public void init() {
        pterodactyl = new PteroBuilder()
                .setApplicationUrl(config.PTERODACTYL_CONFIG.APPLICATION_URL)
                .setToken(config.PTERODACTYL_CONFIG.APPLICATION_API_TOKEN)
                .build();
    }

    public void createDungeonInstance(Dungeon dungeon, Consumer<DungeonInstance> instanceConsumer) {
        pterodactyl.asApplication()
                .createServer()
                .setName(dungeon.getName() + calcDungeonInstanceSequenceNumber(dungeon))
                .setMemory(dungeon.getInstanceSettings().getMaxMemory(), DataType.MB)
                .setPack(dungeon.getPackId())
                .startOnCompletion(true)
                .build()
                .executeAsync(applicationServer -> {
                    DungeonInstance instance = new DungeonInstance();
                    instance.setDungeon(dungeon);
                    instance.setApplicationServer(applicationServer);
                    registerDungeonInstance(instance);
                    instanceConsumer.accept(instance);
                });
    }

    private int calcDungeonInstanceSequenceNumber(Dungeon dungeon) {
        return dungeonInstances.getOrDefault(dungeon.getName(), new ArrayList<>()).size();
    }

    private void registerDungeonInstance(DungeonInstance instance) {
        List<DungeonInstance> instances = dungeonInstances.computeIfAbsent(instance.getDungeon().getName(), k -> new ArrayList<>());
        instances.add(instance);
    }
}

package com.atherys.dungeons.model;

import com.mattmalec.pterodactyl4j.application.entities.Allocation;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;

import java.util.function.Consumer;

public class DungeonInstance {

    private String name;

    private Dungeon dungeon;

    private ApplicationServer applicationServer;

    private Allocation allocation;

    private boolean isRegistered;

    private Consumer<DungeonInstance> onRegistration;

    public DungeonInstance() {
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public ApplicationServer getApplicationServer() {
        return applicationServer;
    }

    public void setApplicationServer(ApplicationServer applicationServer) {
        this.applicationServer = applicationServer;
    }


    public Allocation getAllocation() {
        return allocation;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public Consumer<DungeonInstance> getOnRegistration() {
        return onRegistration;
    }

    public void setOnRegistration(Consumer<DungeonInstance> onRegistration) {
        this.onRegistration = onRegistration;
    }
}

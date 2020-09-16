package com.atherys.dungeons.model;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;

public class DungeonInstance {

    private Dungeon dungeon;

    private ApplicationServer applicationServer;

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
}

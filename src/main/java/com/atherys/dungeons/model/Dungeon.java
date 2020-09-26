package com.atherys.dungeons.model;

import com.atherys.dungeons.config.InstanceConfig;
import ninja.leaping.configurate.objectmapping.Setting;

public class Dungeon {

    private long packId;

    private long nestId;

    private long eggId;

    private String name;

    private int minPlayers = 1;

    private int maxPlayer = 5;

    private InstanceSettings instanceSettings;

    public Dungeon() {
    }

    public long getPackId() {
        return packId;
    }

    public void setPackId(long packId) {
        this.packId = packId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public InstanceSettings getInstanceSettings() {
        return instanceSettings;
    }

    public void setInstanceSettings(InstanceSettings instanceSettings) {
        this.instanceSettings = instanceSettings;
    }

    public long getNestId() {
        return nestId;
    }

    public void setNestId(long nestId) {
        this.nestId = nestId;
    }

    public long getEggId() {
        return eggId;
    }

    public void setEggId(long eggId) {
        this.eggId = eggId;
    }
}

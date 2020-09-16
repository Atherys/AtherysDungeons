package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DungeonConfig {

    @Setting("id")
    public String ID;

    @Setting("name")
    public String name;

    @Setting("min-players")
    public int MIN_PLAYERS = 1;

    @Setting("max-players")
    public int MAX_PLAYERS = 5;

    @Setting("instance-settings")
    public InstanceConfig INSTANCE_CONFIG;

}

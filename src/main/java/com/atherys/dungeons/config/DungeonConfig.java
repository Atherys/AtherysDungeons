package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DungeonConfig {

    @Setting("pack-id")
    public long PACK_ID;

    @Setting("name")
    public String NAME;

    @Setting("min-players")
    public int MIN_PLAYERS = 1;

    @Setting("max-players")
    public int MAX_PLAYERS = 5;

    @Setting("instance-settings")
    public InstanceConfig INSTANCE_CONFIG;

}

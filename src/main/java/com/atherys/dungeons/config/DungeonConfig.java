package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DungeonConfig {

    @Setting("pack-id")
    public long PACK_ID;

    @Setting("nest-id")
    public long NEST_ID = 1;

    @Setting("egg-id")
    public long EGG_ID = 12;

    @Setting("name")
    public String NAME;

    @Setting("min-players")
    public int MIN_PLAYERS = 1;

    @Setting("max-players")
    public int MAX_PLAYERS = 5;

    @Setting("instance-settings")
    public InstanceConfig INSTANCE_CONFIG;

}

package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class InstanceConfig {

    // Max RAM to be allocated in MB
    @Setting("max-memory")
    public int MAX_MEMORY = 1024;

}

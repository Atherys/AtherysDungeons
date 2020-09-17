package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class InstanceConfig {

    // Max RAM to be allocated in MB
    @Setting("memory")
    public int MEMORY = 1024;

    @Setting("swap")
    public int SWAP = 2048;

    @Setting("disk")
    public int DISK = 2048;

    @Setting("cpu")
    public int CPU = 1;

    @Setting("port-range")
    public List<Integer> PORT_RANGE;

}

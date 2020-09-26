package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class InstanceConfig {

    // Max RAM to be allocated in MB
    @Setting("memory")
    public int MEMORY = 4096;

    @Setting("swap")
    public int SWAP = 4096;

    @Setting("disk")
    public int DISK = 2048;

    @Setting("cpu")
    public int CPU = 100;

    @Setting("startup-command")
    public String STARTUP_COMMAND = "java -Xms3072M -Xmx3072M -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:-OmitStackTraceInFastThrow -XX:+AlwaysPreTouch  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=8 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=true -Daikars.new.flags=true -jar server.jar";

    @Setting("environment")
    public Map<String, String> ENVIRONMENT = new HashMap<>();
}

package com.atherys.dungeons.model;

import java.util.Map;

public class InstanceSettings {

    private long memory;

    private long cpu;

    private long disk;

    private long swap;

    private String startupCommand;

    private Map<String, String> environment;

    public InstanceSettings() {
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getCpu() {
        return cpu;
    }

    public void setCpu(long cpu) {
        this.cpu = cpu;
    }

    public long getDisk() {
        return disk;
    }

    public void setDisk(long disk) {
        this.disk = disk;
    }

    public long getSwap() {
        return swap;
    }

    public void setSwap(long swap) {
        this.swap = swap;
    }

    public String getStartupCommand() {
        return startupCommand;
    }

    public void setStartupCommand(String startupCommand) {
        this.startupCommand = startupCommand;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }
}

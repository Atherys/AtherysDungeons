package com.atherys.dungeons;

import com.atherys.core.utils.PluginConfig;
import com.atherys.dungeons.config.DungeonConfig;
import com.atherys.dungeons.config.PterodactylConfig;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.Setting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AtherysDungeonsConfig extends PluginConfig {

    @Setting("max-number-of-dungeons")
    public int MAX_NUMBER_OF_INSTANCES = 3;

    @Setting("pterodactyl")
    public PterodactylConfig PTERODACTYL_CONFIG = new PterodactylConfig();

    @Setting("dungeons")
    public List<DungeonConfig> DUNGEONS = new ArrayList<>();

    protected AtherysDungeonsConfig() throws IOException {
        super("config/atherysdungeons", "config.conf");
    }
}

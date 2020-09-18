package com.atherys.dungeons;

import com.atherys.dungeons.facade.DungeonFacade;
import com.atherys.dungeons.facade.DungeonsMessagingFacade;
import com.atherys.dungeons.service.DungeonInstantiationService;
import com.atherys.dungeons.service.PluginMessageService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "atherysdungeons",
        name = "A'therys Dungeons",
        description = "A dungeon instancing and generation plugin for the A'therys Horizons server",
        version = "%PLUGIN_VERSION%",
        dependencies = {
                @Dependency(id = "atheryscore"),
                @Dependency(id = "atherysparties")
        }
)
public class AtherysDungeons {

    private static AtherysDungeons instance;

    private static boolean init = false;

    @Inject
    private Logger logger;

    @Inject
    private Injector spongeInjector;

    private Injector dungeonsInjector;

    private Components components;

    private void init() {
        instance = this;

        components = new Components();
        dungeonsInjector = spongeInjector.createChildInjector();
        dungeonsInjector.injectMembers(components);

        components.config.init();
        components.pluginMessageService.init();
        components.dungeonInstantiationService.init();
        components.dungeonFacade.init();
    }

    private void start() {
    }

    private void reload(Cause cause) {

    }

    private void stop() {
        components.dungeonInstantiationService.stop();
    }

    @Listener(order = Order.LATE)
    public void onInit(GameInitializationEvent event) {
        init();
    }

    @Listener
    public void onStart(GameStartingServerEvent event) {
        if (init) {
            start();
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        if (init) {
            reload(event.getCause());
        }
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        if (init) {
            stop();
        }
    }

    public DungeonFacade getDungeonFacade() {
        return components.dungeonFacade;
    }

    public DungeonsMessagingFacade getDungeonsMessagingFacade() {
        return components.dungeonsMessagingFacade;
    }

    public static AtherysDungeons getInstance() {
        return instance;
    }

    private static class Components {
        @Inject
        AtherysDungeonsConfig config;

        @Inject
        DungeonFacade dungeonFacade;

        @Inject
        DungeonsMessagingFacade dungeonsMessagingFacade;

        @Inject
        DungeonInstantiationService dungeonInstantiationService;

        @Inject
        PluginMessageService pluginMessageService;
    }
}

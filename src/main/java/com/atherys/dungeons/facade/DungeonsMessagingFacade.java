package com.atherys.dungeons.facade;

import com.atherys.core.utils.AbstractMessagingFacade;
import com.google.inject.Singleton;

@Singleton
public class DungeonsMessagingFacade extends AbstractMessagingFacade {
    public DungeonsMessagingFacade() {
        super("Dungeons");
    }
}

package com.lowdragmc.lowdraglib2.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Simple callbacks mirroring container open hooks previously provided by NeoForge.
 */
public final class PlayerContainerEvents {
    private PlayerContainerEvents() {
    }

    public static final Event<Open> OPEN = EventFactory.createArrayBacked(Open.class, listeners -> (player, menu) -> {
        for (Open listener : listeners) {
            listener.onOpen(player, menu);
        }
    });

    @FunctionalInterface
    public interface Open {
        void onOpen(ServerPlayer player, AbstractContainerMenu menu);
    }
}

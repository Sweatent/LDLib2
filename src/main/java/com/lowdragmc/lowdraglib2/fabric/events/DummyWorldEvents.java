package com.lowdragmc.lowdraglib2.fabric.events;

import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;

/**
 * Lightweight callbacks fired by {@link DummyWorld} to provide join and tick hooks on Fabric.
 */
public final class DummyWorldEvents {
    private DummyWorldEvents() {
    }

    public static final Event<EntityJoin> ENTITY_JOIN = EventFactory.createArrayBacked(EntityJoin.class, listeners -> (world, entity) -> {
        for (EntityJoin listener : listeners) {
            if (!listener.onEntityJoin(world, entity)) {
                return false;
            }
        }
        return true;
    });

    public static final Event<EntityTick> ENTITY_TICK_PRE = EventFactory.createArrayBacked(EntityTick.class, listeners -> (world, entity) -> {
        for (EntityTick listener : listeners) {
            if (!listener.onEntityTick(world, entity)) {
                return false;
            }
        }
        return true;
    });

    public static final Event<EntityTickEnd> ENTITY_TICK_POST = EventFactory.createArrayBacked(EntityTickEnd.class, listeners -> (world, entity) -> {
        for (EntityTickEnd listener : listeners) {
            listener.onEntityTick(world, entity);
        }
    });

    @FunctionalInterface
    public interface EntityJoin {
        /**
         * @return {@code true} to allow the entity to be added to the dummy world, {@code false} to skip it.
         */
        boolean onEntityJoin(DummyWorld world, Entity entity);
    }

    @FunctionalInterface
    public interface EntityTick {
        /**
         * @return {@code true} to continue ticking the entity, {@code false} to skip {@link Entity#tick()}.
         */
        boolean onEntityTick(DummyWorld world, Entity entity);
    }

    @FunctionalInterface
    public interface EntityTickEnd {
        void onEntityTick(DummyWorld world, Entity entity);
    }
}

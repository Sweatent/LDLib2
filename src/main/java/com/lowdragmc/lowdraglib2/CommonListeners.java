package com.lowdragmc.lowdraglib2;

import com.lowdragmc.lowdraglib2.async.AsyncThreadData;
import com.lowdragmc.lowdraglib2.editor.resource.PackResourceProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.resources.ResourceType;

/**
 * Registers common lifecycle hooks using Fabric events.
 */
public final class CommonListeners {

    private CommonListeners() {
    }

    public static void init() {
        ServerWorldEvents.UNLOAD.register((server, level) -> releaseAsyncResources(level));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> server.getAllLevels()
                .forEach(CommonListeners::releaseAsyncResources));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ServerCommands.createServerCommands().forEach(dispatcher::register));

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(PackResourceProvider.Manager.INSTANCE);
    }

    private static void releaseAsyncResources(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            AsyncThreadData.getOrCreate(serverLevel).releaseExecutorService();
        }
    }
}

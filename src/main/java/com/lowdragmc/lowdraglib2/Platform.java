package com.lowdragmc.lowdraglib2;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

public class Platform {

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    // This is a helper method to check if the ServerLevel is safe to access.
    // @return true if the ServerLevel is not safe to access, otherwise false.
    public static boolean isServerNotSafe() {
        if (Platform.isClient()) {
            return Minecraft.getInstance().getConnection() == null;
        } else {
            var server = getMinecraftServer();
            return server == null || server.isStopped() || server.isShutdown() || !server.isRunning() || server.isCurrentlySaving();
        }
    }
    @ApiStatus.Internal
    public static RegistryAccess FROZEN_REGISTRY_ACCESS = BLANK;


    private static MinecraftServer currentServer;
    private static boolean lifecycleBound;

    public static void init() {
        if (lifecycleBound) {
            return;
        }
        lifecycleBound = true;
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            currentServer = server;
            FROZEN_REGISTRY_ACCESS = server.registryAccess();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (currentServer == server) {
                currentServer = null;
            }
            FROZEN_REGISTRY_ACCESS = null;
        });
    }

    public static String platformName() {
        return "Fabric";
    }

    public static boolean isForge() {
        return false;
    }

    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isDatagen() {
        return Boolean.getBoolean("fabric-api.datagen");
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static MinecraftServer getMinecraftServer() {
        return currentServer;
    }

    public static Path getGamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static RegistryAccess getFrozenRegistry() {
        if (FROZEN_REGISTRY_ACCESS != null) {
            return FROZEN_REGISTRY_ACCESS;
        } else if (LDLib2.isRemote()) {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            }
        }
        return BLANK;
    }

}

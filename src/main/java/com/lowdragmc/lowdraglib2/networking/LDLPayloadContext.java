package com.lowdragmc.lowdraglib2.networking;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface LDLPayloadContext {
    Player player();
}

package com.lowdragmc.lowdraglib2.networking.compat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * A lightweight replacement for {@link net.minecraft.network.RegistryFriendlyByteBuf}
 * that retains access to the registry lookup while delegating all buffer
 * operations to Fabric's {@link PacketByteBuf} implementation.
 */
public class CompatRegistryFriendlyByteBuf extends PacketByteBuf {
    private final RegistryAccess registryAccess;

    private CompatRegistryFriendlyByteBuf(ByteBuf parent, RegistryAccess registryAccess) {
        super(parent);
        this.registryAccess = registryAccess;
    }

    private CompatRegistryFriendlyByteBuf(PacketByteBuf parent, RegistryAccess registryAccess) {
        super(parent);
        this.registryAccess = registryAccess;
    }

    public static CompatRegistryFriendlyByteBuf create(RegistryAccess registryAccess) {
        return new CompatRegistryFriendlyByteBuf(PacketByteBufs.create(), registryAccess);
    }

    public static CompatRegistryFriendlyByteBuf wrap(byte[] data, RegistryAccess registryAccess) {
        PacketByteBuf delegate = PacketByteBufs.copy(Unpooled.wrappedBuffer(data));
        delegate.readerIndex(0);
        return new CompatRegistryFriendlyByteBuf(delegate, registryAccess);
    }

    public static CompatRegistryFriendlyByteBuf wrap(PacketByteBuf buf, RegistryAccess registryAccess) {
        return new CompatRegistryFriendlyByteBuf(buf, registryAccess);
    }

    public static CompatRegistryFriendlyByteBuf wrap(ByteBuf buf, RegistryAccess registryAccess) {
        return new CompatRegistryFriendlyByteBuf(buf, registryAccess);
    }

    public static CompatRegistryFriendlyByteBuf wrap(RegistryFriendlyByteBuf buf) {
        return wrap((ByteBuf) buf, buf.registryAccess());
    }

    public RegistryAccess registryAccess() {
        return registryAccess;
    }
}

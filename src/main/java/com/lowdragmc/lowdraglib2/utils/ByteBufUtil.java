package com.lowdragmc.lowdraglib2.utils;

import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import lombok.experimental.UtilityClass;
import net.minecraft.core.RegistryAccess;

import java.util.function.Consumer;

@UtilityClass
public final class ByteBufUtil {
    /**
     * Writes custom data to a {@link CompatRegistryFriendlyByteBuf}, then read it for consumer.
     *
     * @param data           Data to write.
     * @param dataWriter     The data reader.
     * @param registryAccess The registry access used by registry dependent writers on the buffer
     */
    public static void readCustomData(byte[] data, Consumer<CompatRegistryFriendlyByteBuf> dataWriter, RegistryAccess registryAccess) {
        final CompatRegistryFriendlyByteBuf buf = CompatRegistryFriendlyByteBuf.wrap(data, registryAccess);
        try {
            dataWriter.accept(buf);
        } finally {
            buf.release();
        }
    }

    /**
     * Writes custom data to a {@link CompatRegistryFriendlyByteBuf}, then returns the written data as a byte array.
     *
     * @param dataWriter     The data writer.
     * @param registryAccess The registry access used by registry dependent writers on the buffer
     * @return The written data.
     */
    public static byte[] writeCustomData(Consumer<CompatRegistryFriendlyByteBuf> dataWriter, RegistryAccess registryAccess) {
        final CompatRegistryFriendlyByteBuf buf = CompatRegistryFriendlyByteBuf.create(registryAccess);
        try {
            dataWriter.accept(buf);
            buf.readerIndex(0);
            final byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } finally {
            buf.release();
        }
    }
}

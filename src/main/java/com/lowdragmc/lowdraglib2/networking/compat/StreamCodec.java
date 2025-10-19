package com.lowdragmc.lowdraglib2.networking.compat;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A lightweight replica of Minecraft's {@code StreamCodec} interface that operates on
 * {@link CompatRegistryFriendlyByteBuf} instances. The API surface mirrors the 1.21
 * methods so that call sites using method references or lambdas continue to compile.
 */
public interface StreamCodec<B extends ByteBuf, V> {
    void encode(B buf, V value);

    V decode(B buf);

    static <B extends ByteBuf, V> StreamCodec<B, V> of(
            BiConsumer<? super B, ? super V> writer,
            Function<? super B, ? extends V> reader
    ) {
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(reader, "reader");
        return new StreamCodec<>() {
            @Override
            public void encode(B buf, V value) {
                writer.accept(buf, value);
            }

            @Override
            public V decode(B buf) {
                return reader.apply(buf);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, V> ofMember(
            BiConsumer<? super V, ? super B> writer,
            Function<? super B, ? extends V> reader
    ) {
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(reader, "reader");
        return new StreamCodec<>() {
            @Override
            public void encode(B buf, V value) {
                writer.accept(value, buf);
            }

            @Override
            public V decode(B buf) {
                return reader.apply(buf);
            }
        };
    }
}

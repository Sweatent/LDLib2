package com.lowdragmc.lowdraglib2.networking.compat;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamCodecTest {
    @Test
    void ofMemberSupportsInstanceMethodReferences() {
        StreamCodec<CompatRegistryFriendlyByteBuf, DummyPayload> codec =
                StreamCodec.ofMember(DummyPayload::write, DummyPayload::decode);

        DummyPayload payload = new DummyPayload(42);
        CompatRegistryFriendlyByteBuf buffer =
                CompatRegistryFriendlyByteBuf.wrap(Unpooled.buffer(), RegistryAccess.EMPTY);

        codec.encode(buffer, payload);
        buffer.readerIndex(0);
        DummyPayload decoded = codec.decode(buffer);

        assertEquals(payload.value, decoded.value);
    }

    private static final class DummyPayload {
        private final int value;

        private DummyPayload(int value) {
            this.value = value;
        }

        private void write(CompatRegistryFriendlyByteBuf buf) {
            buf.writeVarInt(value);
        }

        private static DummyPayload decode(CompatRegistryFriendlyByteBuf buf) {
            return new DummyPayload(buf.readVarInt());
        }
    }
}

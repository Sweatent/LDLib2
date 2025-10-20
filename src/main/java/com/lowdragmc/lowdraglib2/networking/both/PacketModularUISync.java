package com.lowdragmc.lowdraglib2.networking.both;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.IUISyncManagerHolder;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import lombok.NoArgsConstructor;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.lowdragmc.lowdraglib2.networking.LDLPayloadContext;

import javax.annotation.Nonnull;

/**
 * a packet that contains payload for managed fields
 */
@NoArgsConstructor
public class PacketModularUISync implements CustomPacketPayload {
    public static final ResourceLocation ID = LDLib2.id("modular_ui_sync");
    public static final Type<PacketModularUISync> TYPE = new Type<>(ID);
    public static final StreamCodec<CompatRegistryFriendlyByteBuf, PacketModularUISync> CODEC = StreamCodec.ofMember(PacketModularUISync::write, PacketModularUISync::decode);

    private byte[] data;

    public PacketModularUISync(byte[] data) {
        this.data = data;
    }

    public void write(CompatRegistryFriendlyByteBuf buf) {
        buf.writeByteArray(data);
    }

    public static PacketModularUISync decode(CompatRegistryFriendlyByteBuf buffer) {
        var data = buffer.readByteArray();
        return new PacketModularUISync(data);
    }

    public static void execute(PacketModularUISync packet, LDLPayloadContext context) {
        if (context.player() instanceof ServerPlayer) {
            executeServer(packet, context);
        } else {
            executeClient(packet, context);
        }
    }

    public static void executeClient(PacketModularUISync packet, LDLPayloadContext context) {
        var player = context.player();
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            ByteBufUtil.readCustomData(packet.data,
                    buf -> syncManagerHolder.getSyncManager().handleSyncPacket(buf),
                    context.player().registryAccess());
        }
    }

    public static void executeServer(PacketModularUISync packet, LDLPayloadContext context) {
        var player = context.player();
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            ByteBufUtil.readCustomData(packet.data,
                    buf -> syncManagerHolder.getSyncManager().handleSyncPacket(buf),
                    context.player().registryAccess());
        }
    }

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

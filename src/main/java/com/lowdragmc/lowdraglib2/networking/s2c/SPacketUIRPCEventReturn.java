package com.lowdragmc.lowdraglib2.networking.s2c;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib2.gui.sync.IUISyncManagerHolder;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import com.lowdragmc.lowdraglib2.networking.compat.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.lowdragmc.lowdraglib2.networking.LDLPayloadContext;

@NoArgsConstructor
public class SPacketUIRPCEventReturn implements CustomPacketPayload {
    public static final ResourceLocation ID = LDLib2.id("ui_rpc_event_return");
    public static final Type<SPacketUIRPCEventReturn> TYPE = new Type<>(ID);
    public static final StreamCodec<CompatRegistryFriendlyByteBuf, SPacketUIRPCEventReturn> CODEC = StreamCodec.ofMember(SPacketUIRPCEventReturn::write, SPacketUIRPCEventReturn::decode);

    public byte[] returnData;

    public SPacketUIRPCEventReturn(byte[] returnData) {
        this.returnData = returnData;
    }

    public void write(CompatRegistryFriendlyByteBuf buf) {
        buf.writeByteArray(returnData);
    }

    public static SPacketUIRPCEventReturn decode(CompatRegistryFriendlyByteBuf buf) {
        var returnData = buf.readByteArray();
        return new SPacketUIRPCEventReturn(returnData);
    }

    public static void execute(SPacketUIRPCEventReturn packet, LDLPayloadContext context) {
        var player = context.player();
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            ByteBufUtil.readCustomData(packet.returnData,
                    buf -> syncManagerHolder.getSyncManager().handEventReturn(buf),
                    context.player().registryAccess());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

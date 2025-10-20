package com.lowdragmc.lowdraglib2.networking.c2s;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.modular.ModularUIContainer;
import lombok.NoArgsConstructor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;

@NoArgsConstructor
public class CPacketUIClientAction implements CustomPacketPayload {
    public static final ResourceLocation ID = LDLib2.id("ui_client_action");
    public static final Type<CPacketUIClientAction> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketUIClientAction> CODEC = StreamCodec.ofMember(CPacketUIClientAction::write, CPacketUIClientAction::decode);
    public int windowId;
    public byte[] updateData;

    public CPacketUIClientAction(int windowId, byte[] updateData) {
        this.windowId = windowId;
        this.updateData = updateData;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        CompatRegistryFriendlyByteBuf compatBuf = CompatRegistryFriendlyByteBuf.wrap(buf);
        compatBuf.writeVarInt(windowId);
        compatBuf.writeByteArray(updateData);
    }

    public static CPacketUIClientAction decode(RegistryFriendlyByteBuf buf) {
        CompatRegistryFriendlyByteBuf compatBuf = CompatRegistryFriendlyByteBuf.wrap(buf);
        var windowId = compatBuf.readVarInt();
        var updateData = compatBuf.readByteArray();
        return new CPacketUIClientAction(windowId, updateData);
    }

    public static void execute(CPacketUIClientAction packet, IPayloadContext context) {
        AbstractContainerMenu openContainer = context.player().containerMenu;
        if (openContainer instanceof ModularUIContainer) {
            ((ModularUIContainer)openContainer).handleClientAction(packet);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.lowdragmc.lowdraglib2.networking.s2c;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.modular.ModularUIGuiContainer;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;

@NoArgsConstructor
public class SPacketUIWidgetUpdate implements CustomPacketPayload {
    public static final ResourceLocation ID = LDLib2.id("ui_widget_update");
    public static final Type<SPacketUIWidgetUpdate> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketUIWidgetUpdate> CODEC = StreamCodec.ofMember(SPacketUIWidgetUpdate::write, SPacketUIWidgetUpdate::decode);

    public int windowId;
    public byte[] updateData;

    public SPacketUIWidgetUpdate(int windowId, byte[] updateData) {
        this.windowId = windowId;
        this.updateData = updateData;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        CompatRegistryFriendlyByteBuf compatBuf = CompatRegistryFriendlyByteBuf.wrap(buf);
        compatBuf.writeVarInt(windowId);
        compatBuf.writeByteArray(updateData);
    }

    public static SPacketUIWidgetUpdate decode(RegistryFriendlyByteBuf buf) {
        CompatRegistryFriendlyByteBuf compatBuf = CompatRegistryFriendlyByteBuf.wrap(buf);
        var windowId = compatBuf.readVarInt();
        var updateData = compatBuf.readByteArray();
        return new SPacketUIWidgetUpdate(windowId, updateData);
    }

    public static void execute(SPacketUIWidgetUpdate packet, IPayloadContext context) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof ModularUIGuiContainer) {
            ((ModularUIGuiContainer) currentScreen).handleWidgetUpdate(packet);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

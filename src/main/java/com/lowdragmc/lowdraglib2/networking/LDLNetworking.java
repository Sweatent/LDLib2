package com.lowdragmc.lowdraglib2.networking;

import com.lowdragmc.lowdraglib2.networking.both.PacketModularUISync;
import com.lowdragmc.lowdraglib2.networking.both.PacketRPCBlockEntity;
import com.lowdragmc.lowdraglib2.networking.c2s.CPacketUIClientAction;
import com.lowdragmc.lowdraglib2.networking.c2s.CPacketUIRPCEvent;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketUIOpen;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketUIRPCEventReturn;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketUIWidgetUpdate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.BiConsumer;

/**
 * Author: KilaBash
 * Date: 2022/04/27
 * Description:
 */
public class LDLNetworking {

    public static void init() {
        FabricPayloadRegistrar registrar = new FabricPayloadRegistrar();

        registrar.playToClient(SPacketUIOpen.TYPE, SPacketUIOpen.CODEC, SPacketUIOpen::execute);
        registrar.playToClient(SPacketUIWidgetUpdate.TYPE, SPacketUIWidgetUpdate.CODEC, SPacketUIWidgetUpdate::execute);
        registrar.playToClient(SPacketAutoSyncBlockEntity.TYPE, SPacketAutoSyncBlockEntity.CODEC, SPacketAutoSyncBlockEntity::execute);
        registrar.playToClient(SPacketUIRPCEventReturn.TYPE, SPacketUIRPCEventReturn.CODEC, SPacketUIRPCEventReturn::execute);

        registrar.playToServer(CPacketUIClientAction.TYPE, CPacketUIClientAction.CODEC, CPacketUIClientAction::execute);
        registrar.playToServer(CPacketUIRPCEvent.TYPE, CPacketUIRPCEvent.CODEC, CPacketUIRPCEvent::execute);

        registrar.playBidirectional(PacketRPCBlockEntity.TYPE, PacketRPCBlockEntity.CODEC, PacketRPCBlockEntity::execute);
        registrar.playBidirectional(PacketModularUISync.TYPE, PacketModularUISync.CODEC, PacketModularUISync::execute);
    }

    @FunctionalInterface
    private interface PayloadHandler<T extends CustomPacketPayload> extends BiConsumer<T, LDLPayloadContext> {
        @Override
        void accept(T payload, LDLPayloadContext context);
    }

    private static class FabricPayloadRegistrar {

        private <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type, StreamCodec<CompatRegistryFriendlyByteBuf, T> codec, PayloadHandler<T> handler) {
            PayloadTypeRegistry.playToClient().register(type, wrapCodec(codec));
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.execute(() -> {
                        var player = context.client().player;
                        if (player != null) {
                            handler.accept(payload, () -> player);
                        }
                    }));
        }

        private <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type, StreamCodec<CompatRegistryFriendlyByteBuf, T> codec, PayloadHandler<T> handler) {
            PayloadTypeRegistry.playToServer().register(type, wrapCodec(codec));
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.execute(() -> {
                        var player = context.player();
                        if (player != null) {
                            handler.accept(payload, () -> player);
                        }
                    }));
        }

        private <T extends CustomPacketPayload> void playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<CompatRegistryFriendlyByteBuf, T> codec, PayloadHandler<T> handler) {
            playToClient(type, codec, handler);
            playToServer(type, codec, handler);
        }
    }

    private static <T extends CustomPacketPayload> StreamCodec<PacketByteBuf, T> wrapCodec(StreamCodec<CompatRegistryFriendlyByteBuf, T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(PacketByteBuf buf) {
                return codec.decode(CompatRegistryFriendlyByteBuf.wrap(buf, null));
            }

            @Override
            public void encode(PacketByteBuf buf, T value) {
                codec.encode(CompatRegistryFriendlyByteBuf.wrap(buf, null), value);
            }
        };
    }
}

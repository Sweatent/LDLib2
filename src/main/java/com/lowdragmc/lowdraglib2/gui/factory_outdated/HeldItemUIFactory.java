package com.lowdragmc.lowdraglib2.gui.factory_outdated;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib2.gui.modular.ModularUI;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2022/8/26
 * @implNote ItemUIFactory
 */
public class HeldItemUIFactory extends UIFactory<HeldItemUIFactory.HeldItemHolder> {

    public static final HeldItemUIFactory INSTANCE = new HeldItemUIFactory();

    public HeldItemUIFactory() {
        super(LDLib2.id("held_item"));
    }

    public final boolean openUI(ServerPlayer player, InteractionHand hand) {
        return openUI(new HeldItemHolder(player, hand), player);
    }

    @Override
    protected ModularUI createUITemplate(HeldItemHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected HeldItemHolder readHolderFromSyncData(CompatRegistryFriendlyByteBuf syncData) {
        Player player = Minecraft.getInstance().player;
        return player == null ? null :new HeldItemHolder(player, syncData.readEnum(InteractionHand.class));
    }

    @Override
    protected void writeHolderToSyncData(CompatRegistryFriendlyByteBuf syncData, HeldItemHolder holder) {
        syncData.writeEnum(holder.hand);
    }

    @Getter
    public static class HeldItemHolder implements IUIHolder {
        public Player player;
        public InteractionHand hand;
        public ItemStack held;

        public HeldItemHolder(Player player, InteractionHand hand) {
            this.player = player;
            this.hand = hand;
            held = player.getItemInHand(hand);
        }

        @Override
        public ModularUI createUI(Player entityPlayer) {
            if (held.getItem() instanceof IUIHolder.ItemUI itemUIHolder) {
                return itemUIHolder.createUI(entityPlayer, this);
            }
            return null;
        }

        @Override
        public boolean isInvalid() {
            return !ItemStack.isSameItemSameComponents(player.getItemInHand(hand), held);
        }

        @Override
        public boolean isRemote() {
            return player.level().isClientSide;
        }

        @Override
        public void markAsDirty() {

        }

    }

}

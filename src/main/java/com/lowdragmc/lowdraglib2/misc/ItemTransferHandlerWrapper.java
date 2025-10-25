package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter that exposes an {@link ItemTransfer} as an {@link IItemHandlerModifiable} for
 * integrations that still rely on the NeoForge handler API.
 */
final class ItemTransferHandlerWrapper implements IItemHandlerModifiable {
    private final ItemTransfer transfer;

    ItemTransferHandlerWrapper(ItemTransfer transfer) {
        this.transfer = transfer;
    }

    @Override
    public int getSlots() {
        return transfer.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return transfer.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        transfer.setStackInSlot(slot, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return transfer.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return transfer.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return transfer.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return transfer.isItemValid(slot, stack);
    }
}

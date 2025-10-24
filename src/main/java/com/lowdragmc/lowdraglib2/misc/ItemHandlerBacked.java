package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Presents an {@link IItemHandlerModifiable} as the new {@link ItemTransfer} abstraction.
 */
final class ItemHandlerBacked implements ItemTransfer {
    private final IItemHandlerModifiable handler;

    ItemHandlerBacked(IItemHandlerModifiable handler) {
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return handler.isItemValid(slot, stack);
    }

    @Override
    public IItemHandlerModifiable asItemHandler() {
        return handler;
    }
}

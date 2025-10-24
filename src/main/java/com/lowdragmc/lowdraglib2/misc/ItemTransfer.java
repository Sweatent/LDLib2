package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Simple abstraction that mirrors the methods provided by {@link IItemHandlerModifiable}
 * while also allowing conversion to Fabric's {@link Storage} API. Implementations may
 * additionally implement {@link IItemHandlerModifiable} directly when NeoForge compatibility
 * is required. Existing code that still relies on the NeoForge handler can obtain an adapter
 * by calling {@link #asItemHandler()}.
 */
public interface ItemTransfer {

    ItemTransfer EMPTY = new ItemTransfer() {
        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        public IItemHandlerModifiable asItemHandler() {
            return new ItemTransferHandlerWrapper(this);
        }
    };

    int getSlots();

    @NotNull
    ItemStack getStackInSlot(int slot);

    void setStackInSlot(int slot, ItemStack stack);

    @NotNull
    ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

    @NotNull
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, @NotNull ItemStack stack);

    default Storage<ItemVariant> asStorage() {
        return this instanceof Storage<ItemVariant> storage ? storage : new ItemTransferStorageWrapper(this);
    }

    default IItemHandlerModifiable asItemHandler() {
        return this instanceof IItemHandlerModifiable modifiable ? modifiable : new ItemTransferHandlerWrapper(this);
    }

    static ItemTransfer of(IItemHandlerModifiable handler) {
        Objects.requireNonNull(handler, "handler");
        return handler instanceof ItemTransfer transfer ? transfer : new ItemHandlerBacked(handler);
    }

    static ItemTransfer empty() {
        return EMPTY;
    }
}

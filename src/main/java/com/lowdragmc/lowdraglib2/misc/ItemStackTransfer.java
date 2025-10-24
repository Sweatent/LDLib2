package com.lowdragmc.lowdraglib2.misc;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.syncdata.IContentChangeAware;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class ItemStackTransfer extends FabricItemContainer implements IContentChangeAware {
    @Getter
    @Setter
    private Runnable OnContentsChanged = Runnables.doNothing();

    @Setter
    private Function<ItemStack, Boolean> filter;

    public ItemStackTransfer() {
        this(1);
    }

    public ItemStackTransfer(int size) {
        super(size);
    }

    public ItemStackTransfer(NonNullList<ItemStack> stacks) {
        super(stacks.toArray(ItemStack[]::new), 64);
    }

    public ItemStackTransfer(ItemStack stack) {
        this(NonNullList.of(ItemStack.EMPTY, stack));
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack, boolean notify) {
        if (notify) {
            super.setStackInSlot(slot, stack);
        } else {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(copy.getCount(), getSlotLimit(slot)));
            this.stacks[slot] = copy;
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return filter == null || filter.apply(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        OnContentsChanged.run();
    }

    public ItemStackTransfer copy() {
        var copiedStack = NonNullList.withSize(getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < getSlots(); i++) {
            copiedStack.set(i, getStackInSlot(i).copy());
        }
        var copied = new ItemStackTransfer(copiedStack);
        copied.setFilter(filter);
        return copied;
    }

    /**
     * Returns a delegating {@link ItemStackHandler} for integrations that still require
     * NeoForge's concrete container implementation.
     */
    public ItemStackHandler asItemStackHandler() {
        return new DelegatingHandler(this);
    }

    private static final class DelegatingHandler extends ItemStackHandler {
        private final ItemStackTransfer transfer;

        private DelegatingHandler(ItemStackTransfer transfer) {
            super(transfer.getSlots());
            this.transfer = transfer;
        }

        @Override
        public int getSlots() {
            return transfer.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return transfer.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            transfer.setStackInSlot(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return transfer.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return transfer.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return transfer.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return transfer.isItemValid(slot, stack);
        }
    }
}

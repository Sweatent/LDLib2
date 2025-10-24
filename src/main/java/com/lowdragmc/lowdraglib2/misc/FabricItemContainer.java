package com.lowdragmc.lowdraglib2.misc;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Lightweight Fabric compatible container that mimics the behaviour of NeoForge's
 * {@code ItemStackHandler}. Transactions are applied immediately which is sufficient for
 * exposing data to UI integrations and simple automation.
 */
public class FabricItemContainer implements ItemTransfer, IItemHandlerModifiable, Storage<ItemVariant> {

    protected final ItemStack[] stacks;
    private Predicate<ItemStack> filter = stack -> true;
    private final int slotLimit;

    public FabricItemContainer(int size) {
        this(size, 64);
    }

    public FabricItemContainer(int size, int slotLimit) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be non-negative");
        }
        this.slotLimit = slotLimit;
        this.stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
    }

    public FabricItemContainer(ItemStack[] stacks, int slotLimit) {
        this.stacks = Objects.requireNonNull(stacks, "stacks");
        this.slotLimit = slotLimit;
    }

    public void setFilter(Predicate<ItemStack> filter) {
        this.filter = filter == null ? stack -> true : filter;
    }

    protected void onContentsChanged(int slot) {
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.length) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + stacks.length + ")");
        }
    }

    @Override
    public int getSlots() {
        return stacks.length;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks[slot];
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(copy.getCount(), getSlotLimit(slot)));
        stacks[slot] = copy;
        onContentsChanged(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack existing = stacks[slot];
        int limit = getStackLimit(slot, stack);
        if (existing.isEmpty()) {
            int toInsert = Math.min(limit, stack.getCount());
            if (!simulate) {
                stacks[slot] = stack.copyWithCount(toInsert);
                onContentsChanged(slot);
            }
            if (stack.getCount() > toInsert) {
                ItemStack remainder = stack.copy();
                remainder.shrink(toInsert);
                return remainder;
            }
            return ItemStack.EMPTY;
        }
        if (!ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }
        int toInsert = Math.min(limit - existing.getCount(), stack.getCount());
        if (toInsert <= 0) {
            return stack;
        }
        if (!simulate) {
            existing.grow(toInsert);
            onContentsChanged(slot);
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(toInsert);
        return remainder;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        ItemStack existing = stacks[slot];
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(toExtract);
        if (!simulate) {
            if (toExtract == existing.getCount()) {
                stacks[slot] = ItemStack.EMPTY;
            } else {
                existing.shrink(toExtract);
            }
            onContentsChanged(slot);
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return filter.test(stack);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long inserted = 0;
        for (int slot = 0; slot < stacks.length && inserted < maxAmount; slot++) {
            ItemStack toInsert = resource.toStack((int) Math.min(Integer.MAX_VALUE, maxAmount - inserted));
            ItemStack remainder = insertItem(slot, toInsert, false);
            inserted += toInsert.getCount() - remainder.getCount();
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long extracted = 0;
        for (int slot = 0; slot < stacks.length && extracted < maxAmount; slot++) {
            ItemStack existing = stacks[slot];
            if (existing.isEmpty() || !ItemVariant.of(existing).equals(resource)) {
                continue;
            }
            int toExtract = (int) Math.min(maxAmount - extracted, existing.getCount());
            extracted += extractItem(slot, toExtract, false).getCount();
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < stacks.length;
            }

            @Override
            public StorageView<ItemVariant> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new SlotView(index++);
            }
        };
    }

    private final class SlotView implements StorageView<ItemVariant> {
        private final int slot;

        private SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0) {
                return 0;
            }
            ItemStack existing = stacks[slot];
            if (existing.isEmpty() || !ItemVariant.of(existing).equals(resource)) {
                return 0;
            }
            int toExtract = (int) Math.min(maxAmount, existing.getCount());
            return FabricItemContainer.this.extractItem(slot, toExtract, false).getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return stacks[slot].isEmpty();
        }

        @NotNull
        @Override
        public ItemVariant getResource() {
            ItemStack stack = stacks[slot];
            return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
        }

        @Override
        public long getAmount() {
            return stacks[slot].getCount();
        }

        @Override
        public long getCapacity() {
            ItemStack stack = stacks[slot];
            return stack.isEmpty() ? getSlotLimit(slot) : Math.max(stack.getCount(), getSlotLimit(slot));
        }
    }
}

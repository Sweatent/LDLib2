package com.lowdragmc.lowdraglib2.misc;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import com.lowdragmc.lowdraglib2.syncdata.IContentChangeAware;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A Fabric-transfer backed {@link IItemHandlerModifiable} implementation that mirrors the
 * behaviour of NeoForge's {@code ItemStackHandler} while exposing {@link Storage<ItemVariant>} views.
 */
public class FabricItemTransfer implements IItemHandlerModifiable, IContentChangeAware, CompoundTagSerializable {

    private final List<FabricItemSlot> slots = new ArrayList<>();
    private CombinedStorage<ItemVariant, FabricItemSlot> storage = new CombinedStorage<>(slots);
    private Predicate<ItemStack> filter = stack -> true;
    private boolean allowInsert = true;
    private boolean allowExtract = true;
    private Runnable onContentsChanged = Runnables.doNothing();

    public FabricItemTransfer() {
        this(1);
    }

    public FabricItemTransfer(int size) {
        resize(size);
    }

    public FabricItemTransfer(NonNullList<ItemStack> stacks) {
        resize(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            slots.get(i).setStackInternal(stacks.get(i), false);
        }
    }

    public Storage<ItemVariant> asStorage() {
        return storage;
    }

    public FabricItemTransfer setFilter(Predicate<ItemStack> filter) {
        this.filter = Objects.requireNonNull(filter);
        return this;
    }

    public FabricItemTransfer setAllowInsert(boolean allowInsert) {
        this.allowInsert = allowInsert;
        return this;
    }

    public FabricItemTransfer setAllowExtract(boolean allowExtract) {
        this.allowExtract = allowExtract;
        return this;
    }

    public void setSlotLimit(int slot, int limit) {
        validateSlotIndex(slot);
        slots.get(slot).setLimit(limit);
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return slots.get(slot).asStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        if (!allowInsert || !isItemValid(slot, stack)) {
            return stack;
        }
        FabricItemSlot storage = slots.get(slot);
        ItemVariant variant = ItemVariant.of(stack);
        long toInsert = stack.getCount();
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(variant, toInsert, transaction);
            if (inserted <= 0) {
                return stack;
            }
            if (!simulate) {
                transaction.commit();
            }
            if (inserted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - (int) inserted);
            return remainder;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        if (!allowExtract) {
            return ItemStack.EMPTY;
        }
        FabricItemSlot storage = slots.get(slot);
        if (storage.isResourceBlank()) {
            return ItemStack.EMPTY;
        }
        ItemVariant variant = storage.getResource();
        long toExtract = Math.min(amount, storage.getAmount());
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(variant, toExtract, transaction);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                transaction.commit();
            }
            ItemStack result = variant.toStack((int) extracted);
            return result;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return slots.get(slot).getLimit();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return filter.test(stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        setStackInSlot(slot, stack, true);
    }

    public void setStackInSlot(int slot, ItemStack stack, boolean notify) {
        validateSlotIndex(slot);
        slots.get(slot).setStackInternal(stack, notify);
    }

    @Override
    public void setOnContentsChanged(Runnable onContentChanged) {
        this.onContentsChanged = onContentChanged == null ? Runnables.doNothing() : onContentChanged;
    }

    @Override
    public Runnable getOnContentsChanged() {
        return onContentsChanged;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).asStack();
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", i);
                slotTag.putInt("Limit", slots.get(i).getLimit());
                stack.save(provider, slotTag);
                items.add(slotTag);
            }
        }
        tag.put("Items", items);
        tag.putInt("Size", slots.size());
        tag.putBoolean("AllowInsert", allowInsert);
        tag.putBoolean("AllowExtract", allowExtract);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        int size = tag.contains("Size", Tag.TAG_INT) ? tag.getInt("Size") : slots.size();
        resize(size);
        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag slotTag = listTag.getCompound(i);
            int slot = slotTag.getInt("Slot");
            if (slot >= 0 && slot < slots.size()) {
                slots.get(slot).setLimit(slotTag.contains("Limit") ? slotTag.getInt("Limit") : Item.ABSOLUTE_MAX_STACK_SIZE);
                ItemStack.parse(provider, slotTag).ifPresent(stack -> slots.get(slot).setStackInternal(stack, false));
            }
        }
        allowInsert = tag.getBoolean("AllowInsert");
        allowExtract = tag.getBoolean("AllowExtract");
    }

    private void resize(int size) {
        slots.clear();
        for (int i = 0; i < size; i++) {
            slots.add(new FabricItemSlot(i));
        }
        storage = new CombinedStorage<>(slots);
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= slots.size()) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");
        }
    }

    private void onSlotChanged() {
        onContentsChanged.run();
    }

    private class FabricItemSlot extends SingleItemStorage {
        private final int index;
        private int limit = Item.ABSOLUTE_MAX_STACK_SIZE;

        private FabricItemSlot(int index) {
            this.index = index;
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return limit;
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            return allowInsert && isItemValid(index, variant.toStack());
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return allowExtract;
        }

        @Override
        protected void onFinalCommit() {
            onSlotChanged();
        }

        private void setLimit(int limit) {
            this.limit = Math.max(0, limit);
        }

        private int getLimit() {
            return limit;
        }

        private void setStackInternal(ItemStack stack, boolean notify) {
            if (stack == null || stack.isEmpty()) {
                this.variant = ItemVariant.blank();
                this.amount = 0;
            } else {
                this.variant = ItemVariant.of(stack);
                this.amount = Math.min(stack.getCount(), limit);
            }
            if (notify) {
                onFinalCommit();
            }
        }

        private ItemStack asStack() {
            if (variant == null || variant.isBlank() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            return variant.toStack((int) Math.min(amount, Integer.MAX_VALUE));
        }
    }
}

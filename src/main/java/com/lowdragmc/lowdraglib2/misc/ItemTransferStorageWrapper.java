package com.lowdragmc.lowdraglib2.misc;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Presents an {@link ItemTransfer} as a Fabric {@link Storage}. This implementation performs
 * simple best-effort conversions and is intended primarily for exposing data to other APIs.
 */
final class ItemTransferStorageWrapper implements Storage<ItemVariant> {

    private final ItemTransfer transfer;

    ItemTransferStorageWrapper(ItemTransfer transfer) {
        this.transfer = transfer;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long inserted = 0;
        for (int slot = 0; slot < transfer.getSlots() && inserted < maxAmount; slot++) {
            ItemStack stack = resource.toStack((int) Math.min(Integer.MAX_VALUE, maxAmount - inserted));
            ItemStack remainder = transfer.insertItem(slot, stack, true);
            long accepted = stack.getCount() - remainder.getCount();
            if (accepted > 0) {
                if (transaction == null) {
                    transfer.insertItem(slot, stack, false);
                } else {
                    transaction.addCloseCallback((t, result) -> {
                        if (result.wasCommitted()) {
                            transfer.insertItem(slot, stack, false);
                        }
                    });
                }
                inserted += accepted;
            }
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long extracted = 0;
        for (int slot = 0; slot < transfer.getSlots() && extracted < maxAmount; slot++) {
            ItemStack inSlot = transfer.getStackInSlot(slot);
            if (inSlot.isEmpty() || !ItemVariant.of(inSlot).equals(resource)) {
                continue;
            }
            int toExtract = (int) Math.min(maxAmount - extracted, Integer.MAX_VALUE);
            if (transaction == null) {
                extracted += transfer.extractItem(slot, toExtract, false).getCount();
            } else {
                transaction.addCloseCallback((t, result) -> {
                    if (result.wasCommitted()) {
                        transfer.extractItem(slot, toExtract, false);
                    }
                });
                extracted += Math.min(toExtract, inSlot.getCount());
            }
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < transfer.getSlots();
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

    private final class SlotView implements SingleSlotStorage<ItemVariant> {
        private final int slot;

        private SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0) {
                return 0;
            }
            ItemStack stack = resource.toStack((int) Math.min(Integer.MAX_VALUE, maxAmount));
            ItemStack remainder = transfer.insertItem(slot, stack, true);
            long accepted = stack.getCount() - remainder.getCount();
            if (accepted <= 0) {
                return 0;
            }
            if (transaction == null) {
                transfer.insertItem(slot, stack, false);
            } else {
                transaction.addCloseCallback((t, result) -> {
                    if (result.wasCommitted()) {
                        transfer.insertItem(slot, stack, false);
                    }
                });
            }
            return accepted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0) {
                return 0;
            }
            ItemStack inSlot = transfer.getStackInSlot(slot);
            if (inSlot.isEmpty() || !ItemVariant.of(inSlot).equals(resource)) {
                return 0;
            }
            int toExtract = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            if (transaction == null) {
                return transfer.extractItem(slot, toExtract, false).getCount();
            }
            transaction.addCloseCallback((t, result) -> {
                if (result.wasCommitted()) {
                    transfer.extractItem(slot, toExtract, false);
                }
            });
            return Math.min(toExtract, inSlot.getCount());
        }

        @Override
        public boolean isResourceBlank() {
            return transfer.getStackInSlot(slot).isEmpty();
        }

        @NotNull
        @Override
        public ItemVariant getResource() {
            ItemStack stack = transfer.getStackInSlot(slot);
            return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
        }

        @Override
        public long getAmount() {
            return transfer.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            ItemStack stack = transfer.getStackInSlot(slot);
            int limit = transfer.getSlotLimit(slot);
            if (stack.isEmpty()) {
                return limit;
            }
            return Math.max(stack.getCount(), limit);
        }
    }
}

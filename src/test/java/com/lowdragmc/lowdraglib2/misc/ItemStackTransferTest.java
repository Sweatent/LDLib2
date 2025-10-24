package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemStackTransferTest {

    @Test
    void insertExtractRespectsStackSizes() {
        ItemStackTransfer transfer = new ItemStackTransfer(1);
        ItemStack apples = new ItemStack(Items.APPLE, 32);

        ItemStack remainder = transfer.insertItem(0, apples.copy(), false);
        assertTrue(remainder.isEmpty(), "All apples should fit in the slot");
        assertEquals(32, transfer.getStackInSlot(0).getCount(), "Inserted count should be stored");

        ItemStack extracted = transfer.extractItem(0, 10, false);
        assertEquals(10, extracted.getCount(), "Extraction should return requested count");
        assertEquals(22, transfer.getStackInSlot(0).getCount(), "Slot should lose extracted amount");
    }

    @Test
    void filterPreventsDisallowedItems() {
        ItemStackTransfer transfer = new ItemStackTransfer(1);
        transfer.setFilter(stack -> stack.is(Items.APPLE));

        ItemStack dirt = new ItemStack(Items.DIRT, 5);
        ItemStack rejected = transfer.insertItem(0, dirt, false);
        assertEquals(dirt, rejected, "Filter should reject non apples");
        assertTrue(transfer.getStackInSlot(0).isEmpty(), "Slot should remain empty after rejection");

        ItemStack apple = new ItemStack(Items.APPLE, 4);
        ItemStack accepted = transfer.insertItem(0, apple, false);
        assertTrue(accepted.isEmpty(), "Apples should be accepted by filter");
        assertEquals(4, transfer.getStackInSlot(0).getCount());
    }

    @Test
    void serializeRoundTripPreservesStacks() {
        ItemStackTransfer transfer = new ItemStackTransfer(2);
        transfer.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        transfer.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 7));

        CompoundTag tag = transfer.serializeNBT();
        ItemStackTransfer copy = new ItemStackTransfer(2);
        copy.deserializeNBT(tag);

        assertEquals(transfer.getSlots(), copy.getSlots());
        assertEquals(transfer.getStackInSlot(0), copy.getStackInSlot(0));
        assertEquals(transfer.getStackInSlot(1), copy.getStackInSlot(1));
    }
}

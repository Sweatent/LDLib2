package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricTransferWrappersTest {

    private static HolderLookup.Provider registries;

    @BeforeAll
    static void setup() {
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void fluidTransferSupportsFillDrainFilteringAndSerialization() {
        FabricFluidTransfer transfer = new FabricFluidTransfer(4000)
                .setValidator(stack -> stack.getFluid() == Fluids.WATER);

        int filled = transfer.fill(new FluidStack(Fluids.WATER, 1500), IFluidHandler.FluidAction.EXECUTE);
        assertEquals(1500, filled);
        assertEquals(1500, transfer.getFluidInTank(0).getAmount());

        int rejected = transfer.fill(new FluidStack(Fluids.LAVA, 500), IFluidHandler.FluidAction.EXECUTE);
        assertEquals(0, rejected);
        assertEquals(1500, transfer.getFluidInTank(0).getAmount());

        FluidStack drained = transfer.drain(new FluidStack(Fluids.WATER, 600), IFluidHandler.FluidAction.EXECUTE);
        assertEquals(600, drained.getAmount());
        assertEquals(900, transfer.getFluidInTank(0).getAmount());

        var saved = transfer.serializeNBT(registries);
        FabricFluidTransfer restored = new FabricFluidTransfer(4000).setValidator(stack -> stack.getFluid() == Fluids.WATER);
        restored.deserializeNBT(registries, saved);
        assertEquals(transfer.getFluidInTank(0), restored.getFluidInTank(0));
        assertTrue(restored.supportsDrain(0));
        assertTrue(restored.supportsFill(0));
    }

    @Test
    void itemTransferSupportsInsertExtractFilteringAndSerialization() {
        FabricItemTransfer transfer = new FabricItemTransfer(2)
                .setFilter(stack -> stack.is(Items.APPLE));

        ItemStack inserted = new ItemStack(Items.APPLE, 32);
        ItemStack remainder = transfer.insertItem(0, inserted, false);
        assertTrue(remainder.isEmpty(), "Expected all apples to insert");
        assertEquals(32, transfer.getStackInSlot(0).getCount());

        ItemStack rejected = transfer.insertItem(0, new ItemStack(Items.BEEF, 5), false);
        assertEquals(5, rejected.getCount(), "Filter should reject beef");
        assertEquals(32, transfer.getStackInSlot(0).getCount());

        ItemStack extracted = transfer.extractItem(0, 20, false);
        assertEquals(20, extracted.getCount());
        assertEquals(12, transfer.getStackInSlot(0).getCount());

        var saved = transfer.serializeNBT(registries);
        FabricItemTransfer restored = new FabricItemTransfer()
                .setFilter(stack -> stack.is(Items.APPLE));
        restored.deserializeNBT(registries, saved);
        assertEquals(transfer.getSlots(), restored.getSlots());
        assertEquals(transfer.getStackInSlot(0), restored.getStackInSlot(0));
    }
}

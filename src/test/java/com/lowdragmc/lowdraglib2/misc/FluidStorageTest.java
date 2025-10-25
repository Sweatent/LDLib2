package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidStorageTest {

    @Test
    void fillAndDrainTracksAmount() {
        FluidStorage storage = new FluidStorage(1000);
        FluidStack toInsert = new FluidStack(Fluids.WATER, 600);

        int filled = storage.fill(toInsert, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(600, filled, "Should accept requested amount");
        assertEquals(600, storage.getFluid().getAmount(), "Stored amount should match inserted amount");

        FluidStack drained = storage.drain(250, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(250, drained.getAmount(), "Drain should return requested amount");
        assertEquals(350, storage.getFluid().getAmount(), "Remaining fluid should be reduced by drain amount");
    }

    @Test
    void validatorBlocksMismatchedFluids() {
        FluidStorage storage = new FluidStorage(1000, stack -> stack.getFluid() == Fluids.WATER);

        FluidStack lava = new FluidStack(Fluids.LAVA, 500);
        int filled = storage.fill(lava, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(0, filled, "Validator should prevent inserting lava");
        assertTrue(storage.getFluid().isEmpty(), "Tank should remain empty after rejected insert");

        FluidStack water = new FluidStack(Fluids.WATER, 400);
        int waterFilled = storage.fill(water, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(400, waterFilled, "Allowed fluid should insert successfully");
        assertEquals(400, storage.getFluid().getAmount());
    }

    @Test
    void serializeRoundTripPreservesState() {
        FluidStorage storage = new FluidStorage(1200);
        storage.fill(new FluidStack(Fluids.WATER, 900), IFluidHandler.FluidAction.EXECUTE);

        CompoundTag tag = storage.serializeNBT();
        FluidStorage copy = new FluidStorage(1200);
        copy.deserializeNBT(tag);

        assertEquals(storage.getFluid().getFluid(), copy.getFluid().getFluid(), "Fluids should match after serialization");
        assertEquals(storage.getFluid().getAmount(), copy.getFluid().getAmount(), "Amount should persist across serialization");
        assertEquals(storage.getTankCapacity(0), copy.getTankCapacity(0), "Capacity should round-trip with serialized data");
    }
}

package com.lowdragmc.lowdraglib2.misc;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import com.lowdragmc.lowdraglib2.syncdata.IContentChangeAware;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A Fabric-transfer backed implementation of {@link IFluidHandler} that mirrors
 * the behaviour of NeoForge's {@code FluidTank} while exposing the Fabric storage API.
 */
public class FabricFluidTransfer extends SingleFluidStorage implements IFluidHandlerModifiable, IContentChangeAware, CompoundTagSerializable {

    private final long capacity;
    private Predicate<FluidStack> validator = stack -> true;
    private boolean allowFill = true;
    private boolean allowDrain = true;
    private Runnable onContentsChanged = Runnables.doNothing();

    public FabricFluidTransfer(int capacity) {
        this((long) capacity);
    }

    public FabricFluidTransfer(long capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public FabricFluidTransfer(long capacity, Predicate<FluidStack> validator) {
        this(capacity);
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return capacity;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return allowFill && validator.test(fromVariant(variant, 1));
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return allowDrain;
    }

    @Override
    protected void onFinalCommit() {
        onContentsChanged.run();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        validateTankIndex(tank);
        return toFluidStack(variant, amount);
    }

    @Override
    public int getTankCapacity(int tank) {
        validateTankIndex(tank);
        return (int) Math.min(capacity, Integer.MAX_VALUE);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        validateTankIndex(tank);
        return validator.test(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || !allowFill) {
            return 0;
        }
        if (!validator.test(resource)) {
            return 0;
        }
        FluidVariant variant = toVariant(resource);
        long maxAmount = Math.min(resource.getAmount(), getCapacity(variant));
        if (maxAmount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = insert(variant, maxAmount, transaction);
            if (inserted <= 0) {
                return 0;
            }
            if (action.execute()) {
                transaction.commit();
            }
            return (int) Math.min(inserted, Integer.MAX_VALUE);
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || !allowDrain) {
            return FluidStack.EMPTY;
        }
        FluidVariant desiredVariant = toVariant(resource);
        if (!desiredVariant.equals(this.variant)) {
            return FluidStack.EMPTY;
        }
        long maxAmount = Math.min(resource.getAmount(), amount);
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = extract(desiredVariant, maxAmount, transaction);
            if (extracted <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = toFluidStack(desiredVariant, extracted);
            if (action.execute()) {
                transaction.commit();
            }
            return drained;
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || !allowDrain || variant.isBlank()) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = extract(variant, Math.min(maxDrain, amount), transaction);
            if (extracted <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = toFluidStack(variant, extracted);
            if (action.execute()) {
                transaction.commit();
            }
            return drained;
        }
    }

    @Override
    public void setFluidInTank(int tank, FluidStack fluidStack) {
        setFluidInTank(tank, fluidStack, true);
    }

    public void setFluidInTank(int tank, FluidStack fluidStack, boolean notify) {
        validateTankIndex(tank);
        if (fluidStack == null || fluidStack.isEmpty()) {
            this.variant = FluidVariant.blank();
            this.amount = 0;
        } else {
            this.variant = toVariant(fluidStack);
            this.amount = Math.min(capacity, fluidStack.getAmount());
        }
        if (notify) {
            onFinalCommit();
        }
    }

    @Override
    public boolean supportsFill(int tank) {
        validateTankIndex(tank);
        return allowFill;
    }

    @Override
    public boolean supportsDrain(int tank) {
        validateTankIndex(tank);
        return allowDrain;
    }

    @Override
    public void setOnContentsChanged(Runnable onContentChanged) {
        this.onContentsChanged = onContentChanged == null ? Runnables.doNothing() : onContentChanged;
    }

    @Override
    public Runnable getOnContentsChanged() {
        return onContentsChanged;
    }

    public FabricFluidTransfer setValidator(Predicate<FluidStack> validator) {
        this.validator = Objects.requireNonNull(validator);
        return this;
    }

    public FabricFluidTransfer setAllowFill(boolean allowFill) {
        this.allowFill = allowFill;
        return this;
    }

    public FabricFluidTransfer setAllowDrain(boolean allowDrain) {
        this.allowDrain = allowDrain;
        return this;
    }

    public FluidVariant getVariant() {
        return variant;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AllowFill", allowFill);
        tag.putBoolean("AllowDrain", allowDrain);
        if (!variant.isBlank() && amount > 0) {
            CompoundTag fluidTag = new CompoundTag();
            toFluidStack(variant, amount).save(provider, fluidTag);
            tag.put("Fluid", fluidTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        allowFill = tag.getBoolean("AllowFill");
        allowDrain = tag.getBoolean("AllowDrain");
        if (tag.contains("Fluid")) {
            FluidStack stack = FluidStack.parseOptional(provider, tag.getCompound("Fluid"));
            setFluidInTank(0, stack, false);
        } else {
            setFluidInTank(0, FluidStack.EMPTY, false);
        }
    }

    private void validateTankIndex(int tank) {
        if (tank != 0) {
            throw new IndexOutOfBoundsException("FabricFluidTransfer only exposes a single tank");
        }
    }

    private static FluidVariant toVariant(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return FluidVariant.blank();
        }
        CompoundTag nbt = null;
        if (!stack.isComponentsPatchEmpty()) {
            nbt = DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, stack.getComponentsPatch())
                    .result()
                    .filter(CompoundTag.class::isInstance)
                    .map(CompoundTag.class::cast)
                    .orElse(null);
        }
        return FluidVariant.of(stack.getFluid(), nbt);
    }

    private static FluidStack toFluidStack(FluidVariant variant, long amount) {
        if (variant == null || variant.isBlank() || amount <= 0) {
            return FluidStack.EMPTY;
        }
        int sizedAmount = (int) Math.min(amount, Integer.MAX_VALUE);
        FluidStack stack = new FluidStack(variant.getFluid(), sizedAmount);
        if (variant.getNbt() != null && !variant.getNbt().isEmpty()) {
            DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, variant.getNbt())
                    .result()
                    .ifPresent(stack::applyComponents);
        }
        return stack;
    }

    private static FluidStack fromVariant(FluidVariant variant, int amount) {
        return toFluidStack(variant, amount);
    }
}

package com.lowdragmc.lowdraglib2.utils;

import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import com.lowdragmc.lowdraglib2.networking.compat.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Lightweight wrapper around {@link FluidVariant} that mimics the pieces of
 * NeoForge's {@code FluidStack} API that LDLib widgets rely on. The wrapper
 * keeps the amount as a long (matching Fabric transfer semantics) while still
 * exposing int-based helpers for legacy callers.
 */
public final class FabricFluidStack {
    public static final FabricFluidStack EMPTY = new FabricFluidStack(FluidVariant.blank(), 0);

    public static final Codec<FabricFluidStack> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidVariant.CODEC.fieldOf("variant").forGetter(FabricFluidStack::getVariant),
                    Codec.LONG.fieldOf("amount").forGetter(FabricFluidStack::getAmountLong)
            ).apply(instance, FabricFluidStack::new)
    );

    public static final Codec<FabricFluidStack> OPTIONAL_CODEC = CODEC.optionalFieldOf("value")
            .codec()
            .xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(stack));

    public static final StreamCodec<CompatRegistryFriendlyByteBuf, FabricFluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FabricFluidStack decode(CompatRegistryFriendlyByteBuf buffer) {
            boolean present = buffer.readBoolean();
            if (!present) {
                return FabricFluidStack.EMPTY;
            }
            CompoundTag encoded = buffer.readNbt();
            FluidVariant variant = FluidVariant.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow(false, s -> {});
            long amount = buffer.readLong();
            return new FabricFluidStack(variant, amount);
        }

        @Override
        public void encode(CompatRegistryFriendlyByteBuf buffer, FabricFluidStack value) {
            boolean present = value != null && !value.isEmpty();
            buffer.writeBoolean(present);
            if (!present) {
                return;
            }
            DataResult<CompoundTag> encoded = FluidVariant.CODEC.encodeStart(NbtOps.INSTANCE, value.variant)
                    .map(tag -> tag instanceof CompoundTag compound ? compound : new CompoundTag());
            buffer.writeNbt(encoded.result().orElse(new CompoundTag()));
            buffer.writeLong(value.amount);
        }
    };

    private FluidVariant variant;
    private long amount;

    public FabricFluidStack(FluidVariant variant, long amount) {
        this.variant = Objects.requireNonNull(variant, "variant");
        this.amount = Math.max(0, amount);
        if (this.amount == 0 || variant.isBlank()) {
            this.variant = FluidVariant.blank();
            this.amount = 0;
        }
    }

    public static FabricFluidStack create(FluidVariant variant, long amount) {
        return amount <= 0 || variant.isBlank() ? EMPTY : new FabricFluidStack(variant, amount);
    }

    public static FabricFluidStack of(Fluid fluid, long amount) {
        return create(FluidVariant.of(fluid), amount);
    }

    public static FabricFluidStack of(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        return create(FluidVariant.of(fluid, tag), amount);
    }

    public static FabricFluidStack copyOf(FabricFluidStack stack) {
        return stack == null || stack.isEmpty() ? EMPTY : new FabricFluidStack(stack.variant, stack.amount);
    }

    public static FabricFluidStack fromPacket(CompatRegistryFriendlyByteBuf buffer) {
        return OPTIONAL_STREAM_CODEC.decode(buffer);
    }

    public void toPacket(CompatRegistryFriendlyByteBuf buffer) {
        OPTIONAL_STREAM_CODEC.encode(buffer, this);
    }

    public FluidVariant getVariant() {
        return variant;
    }

    public Fluid getFluid() {
        return variant.getFluid();
    }

    public boolean isEmpty() {
        return amount <= 0 || variant.isBlank();
    }

    public FabricFluidStack copy() {
        return isEmpty() ? EMPTY : new FabricFluidStack(variant, amount);
    }

    public int getAmount() {
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }

    public long getAmountLong() {
        return amount;
    }

    public void setAmount(int amount) {
        setAmount((long) amount);
    }

    public void setAmount(long amount) {
        this.amount = Math.max(0, amount);
        if (this.amount == 0) {
            this.variant = FluidVariant.blank();
        }
    }

    public void grow(int amount) {
        grow((long) amount);
    }

    public void grow(long amount) {
        if (amount <= 0) return;
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        shrink((long) amount);
    }

    public void shrink(long amount) {
        if (amount <= 0) return;
        setAmount(this.amount - amount);
    }

    public CompoundTag getComponentsPatch() {
        return variant.getNbt();
    }

    public DataComponentMap getComponents() {
        CompoundTag nbt = variant.getNbt();
        if (nbt == null) {
            return DataComponentMap.EMPTY;
        }
        return DataComponentMap.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElse(DataComponentMap.EMPTY);
    }

    public void applyComponents(DataComponentMap map) {
        if (map == null || map.isEmpty()) {
            variant = FluidVariant.of(getFluid());
        } else {
            CompoundTag encoded = (CompoundTag) DataComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, map).result().orElse(new CompoundTag());
            variant = FluidVariant.of(getFluid(), encoded);
        }
    }

    public void applyComponents(DataComponentPatch patch) {
        DataComponentMap base = getComponents();
        DataComponentMap patched = base.applyPatch(patch);
        applyComponents(patched);
    }

    public static boolean matches(@Nullable FabricFluidStack left, @Nullable FabricFluidStack right) {
        if (left == null || left.isEmpty()) {
            return right == null || right.isEmpty();
        }
        if (right == null || right.isEmpty()) {
            return false;
        }
        return left.getFluid() == right.getFluid();
    }

    public static boolean isSameFluidSameComponents(@Nullable FabricFluidStack left, @Nullable FabricFluidStack right) {
        if (left == null || right == null) return left == right;
        if (left.isEmpty() || right.isEmpty()) return left.isEmpty() && right.isEmpty();
        return left.variant.equals(right.variant);
    }

    public static FabricFluidStack parseOptional(CompatRegistryFriendlyByteBuf buffer) {
        return OPTIONAL_STREAM_CODEC.decode(buffer);
    }

    public static FabricFluidStack parseOptional(RegistryAccess registryAccess, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return EMPTY;
        }
        if (!tag.contains("fluid")) {
            return EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("fluid"));
        if (id == null) {
            return EMPTY;
        }
        Fluid fluid = registryAccess.registryOrThrow(Registries.FLUID).get(id);
        if (fluid == null) {
            return EMPTY;
        }
        long amount = tag.contains("amount") ? tag.getLong("amount") : tag.getInt("amount");
        CompoundTag components = tag.contains("components") ? tag.getCompound("components") : null;
        return of(fluid, amount, components);
    }

    public CompoundTag save(RegistryAccess registryAccess) {
        CompoundTag tag = new CompoundTag();
        if (isEmpty()) {
            return tag;
        }
        ResourceLocation key = registryAccess.registryOrThrow(Registries.FLUID).getKey(getFluid());
        if (key != null) {
            tag.putString("fluid", key.toString());
        }
        tag.putLong("amount", amount);
        if (variant.getNbt() != null) {
            tag.put("components", variant.getNbt().copy());
        }
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FabricFluidStack that)) return false;
        return amount == that.amount && variant.equals(that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, amount);
    }

    @Override
    public String toString() {
        return "FabricFluidStack{" + getFluid() + " x " + amount + "}";
    }
}

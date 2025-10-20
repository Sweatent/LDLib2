package com.lowdragmc.lowdraglib2.syncdata.accessor.readonly;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class INBTSerializableReadOnlyAccessor implements IReadOnlyAccessor<CompoundTagSerializable> {

    @Override
    public boolean test(Class<?> type) {
        return CompoundTagSerializable.class.isAssignableFrom(type);
    }

    @Override
    public <T> T readReadOnlyValue(DynamicOps<T> op, @NotNull CompoundTagSerializable value) {
        var tag = value.serializeNBT(Platform.getFrozenRegistry());
        return op == NbtOps.INSTANCE ? (T) tag : NbtOps.INSTANCE.convertTo(op, tag);
    }

    @Override
    public <T> void writeReadOnlyValue(DynamicOps<T> op, CompoundTagSerializable value, T payload) {
        Tag tag = op == NbtOps.INSTANCE ? (Tag) payload : op.convertTo(NbtOps.INSTANCE, payload);
        value.deserializeNBT(Platform.getFrozenRegistry(), asCompound(tag));
    }

    @Override
    public void readReadOnlyValueToStream(CompatRegistryFriendlyByteBuf buffer, @NotNull CompoundTagSerializable value) {
        buffer.writeNbt(value.serializeNBT(buffer.registryAccess()));
    }

    @Override
    public void writeReadOnlyValueFromStream(CompatRegistryFriendlyByteBuf buffer, @NotNull CompoundTagSerializable value) {
        var nbt = buffer.readNbt();
        if (nbt != null) {
            value.deserializeNBT(buffer.registryAccess(), nbt);
        }
    }

    private static CompoundTag asCompound(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        var wrapper = new CompoundTag();
        if (tag != null && tag != EndTag.INSTANCE) {
            wrapper.put("_value", tag);
        }
        return wrapper;
    }

}

package com.lowdragmc.lowdraglib2.syncdata.accessor.readonly;

import com.lowdragmc.lowdraglib2.syncdata.IManaged;
import com.lowdragmc.lowdraglib2.syncdata.field.ManagedKey;
import com.lowdragmc.lowdraglib2.syncdata.ref.IManagedReadOnlyRef;
import com.lowdragmc.lowdraglib2.syncdata.var.ReadOnlyVar;
import com.mojang.serialization.DynamicOps;
import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class IManagedObjectAccessor implements IReadOnlyAccessor<IManaged> {

    @Override
    public boolean test(Class<?> type) {
        return IManaged.class.isAssignableFrom(type);
    }

    @Override
    public <T> T readReadOnlyValue(DynamicOps<T> op, @NotNull IManaged value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> void writeReadOnlyValue(DynamicOps<T> op, IManaged value, T payload) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void readReadOnlyValueToStream(CompatRegistryFriendlyByteBuf buffer, @NotNull IManaged value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void writeReadOnlyValueFromStream(CompatRegistryFriendlyByteBuf buffer, @NotNull IManaged value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public IManagedReadOnlyRef createReadOnlyRef(ManagedKey managedKey, ReadOnlyVar<IManaged> field) {
        return new IManagedReadOnlyRef(field, managedKey, this);
    }
}

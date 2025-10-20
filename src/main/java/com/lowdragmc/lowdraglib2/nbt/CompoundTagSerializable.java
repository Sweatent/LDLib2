package com.lowdragmc.lowdraglib2.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * A lightweight replacement for NeoForge's {@code INBTSerializable} that focuses on
 * reading and writing {@link CompoundTag} payloads.
 */
public interface CompoundTagSerializable {

    /**
     * Serializes the object into a {@link CompoundTag}.
     */
    CompoundTag serializeNBT(HolderLookup.Provider provider);

    /**
     * Deserializes the object from the provided {@link CompoundTag}.
     */
    void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag);
}

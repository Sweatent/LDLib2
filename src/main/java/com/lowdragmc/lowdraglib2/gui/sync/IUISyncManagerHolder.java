package com.lowdragmc.lowdraglib2.gui.sync;

import com.lowdragmc.lowdraglib2.networking.compat.CompatRegistryFriendlyByteBuf;

public interface IUISyncManagerHolder {
    UISyncManager getSyncManager();

    default void writeInitialData(CompatRegistryFriendlyByteBuf buf) {
        getSyncManager().writeInitialData(buf);
    }

    default void readInitialData(CompatRegistryFriendlyByteBuf buf) {
        getSyncManager().readInitialData(buf);
    }
}

package com.lowdragmc.lowdraglib2.configurator;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Accessors(chain = true)
public class SerializableRecordAction<T extends CompoundTagSerializable> implements EditAction {
    public final T serializable;
    @Nullable
    @Setter
    private Consumer<T> onExecute;
    @Nullable
    @Setter
    private Consumer<T> onUndo;
    // runtime
    private CompoundTag snapshot;

    private SerializableRecordAction(T serializable) {
        this.serializable = serializable;
        this.snapshot = serializable.serializeNBT(Platform.getFrozenRegistry()).copy();
    }

    public static <T extends CompoundTagSerializable> SerializableRecordAction<T> of(T serializable) {
        return new SerializableRecordAction<>(serializable);
    }

    public SerializableRecordAction<T> setOnAction(@Nullable Consumer<T> onAction) {
        setOnExecute(onAction);
        setOnUndo(onAction);
        return this;
    }

    public void updateSnapshot() {
        snapshot = serializable.serializeNBT(Platform.getFrozenRegistry()).copy();
    }

    @Override
    public void execute() {
        serializable.deserializeNBT(Platform.getFrozenRegistry(), snapshot.copy());
        if (onExecute != null) {
            onExecute.accept(serializable);
        }
    }

    @Override
    public void undo() {
        serializable.deserializeNBT(Platform.getFrozenRegistry(), snapshot.copy());
        if (onUndo != null) {
            onUndo.accept(serializable);
        }
    }
}

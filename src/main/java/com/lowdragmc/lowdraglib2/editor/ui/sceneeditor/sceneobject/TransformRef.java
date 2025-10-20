package com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.sceneobject;

import com.lowdragmc.lowdraglib2.math.Transform;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransformRef implements CompoundTagSerializable {
    @Nullable
    @Getter @Setter
    private UUID transformId = null;

    public TransformRef() {

    }

    public TransformRef(@Nullable Transform transform) {
        this.transformId = transform == null ? null : transform.id();
    }

    public TransformRef(UUID transformId) {
        this.transformId = transformId;
    }

    public void setTransform(Transform transform) {
        this.transformId = transform.id();
    }

    @Nullable
    public Transform getTransform(@Nullable IScene scene) {
        if (transformId == null) return null;
        if (scene == null) return null;
        var obj = scene.getSceneObject(transformId);
        if (obj != null) return obj.transform();
        return null;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        if (transformId != null) {
            tag.putUUID("id", transformId);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("id")) {
            try {
                transformId = nbt.getUUID("id");
            } catch (Exception e) {
                transformId = null;
            }
            return;
        }
        if (nbt.contains("_value")) {
            var legacy = nbt.get("_value");
            if (legacy != null) {
                var legacyValue = legacy.getAsString();
                transformId = legacyValue.isEmpty() ? null : parseUUID(legacyValue);
                return;
            }
        }
        transformId = null;
    }

    @Nullable
    private static UUID parseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        if (transformId == null) return "null";
        return transformId.toString();
    }
}

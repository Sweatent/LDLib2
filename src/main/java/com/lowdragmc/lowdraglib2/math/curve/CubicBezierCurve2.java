package com.lowdragmc.lowdraglib2.math.curve;

import com.lowdragmc.lowdraglib2.math.Interpolations;
import com.lowdragmc.lowdraglib2.nbt.CompoundTagSerializable;
import lombok.EqualsAndHashCode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2f;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
public class CubicBezierCurve2 extends Curve<Vector2f> implements CompoundTagSerializable {
    public Vector2f p0, c0, c1, p1;

    public CubicBezierCurve2(Vector2f start, Vector2f control1, Vector2f control2, Vector2f end) {
        this.p0 = start;
        this.c0 = control1;
        this.c1 = control2;
        this.p1 = end;
    }

    @Override
    public Vector2f getPoint(float t) {
        return new Vector2f(
                (float) Interpolations.CubicBezier(t, p0.x, c0.x, c1.x, p1.x),
                (float) Interpolations.CubicBezier(t, p0.y, c0.y, c1.y, p1.y)
        );
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        var list = new ListTag();
        list.add(FloatTag.valueOf(p0.x));
        list.add(FloatTag.valueOf(p0.y));

        list.add(FloatTag.valueOf(c0.x));
        list.add(FloatTag.valueOf(c0.y));

        list.add(FloatTag.valueOf(c1.x));
        list.add(FloatTag.valueOf(c1.y));

        list.add(FloatTag.valueOf(p1.x));
        list.add(FloatTag.valueOf(p1.y));
        var tag = new CompoundTag();
        tag.put("points", list);
        return tag;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, CompoundTag tag) {
        ListTag list = extractList(tag);
        p0 = new Vector2f(list.getFloat(0), list.getFloat(1));
        c0 = new Vector2f(list.getFloat(2), list.getFloat(3));
        c1 = new Vector2f(list.getFloat(4), list.getFloat(5));
        p1 = new Vector2f(list.getFloat(6), list.getFloat(7));
    }

    public CubicBezierCurve2 copy() {
        return new CubicBezierCurve2(new Vector2f(p0), new Vector2f(c0), new Vector2f(c1), new Vector2f(p1));
    }

    private static ListTag extractList(CompoundTag tag) {
        if (tag.contains("points")) {
            return tag.getList("points", Tag.TAG_FLOAT);
        }
        if (tag.contains("_value") && tag.get("_value") instanceof ListTag legacy) {
            return legacy;
        }
        return new ListTag();
    }
}

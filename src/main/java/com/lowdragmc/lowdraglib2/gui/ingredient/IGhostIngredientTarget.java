package com.lowdragmc.lowdraglib2.gui.ingredient;


import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

public interface IGhostIngredientTarget {

    @Environment(EnvType.CLIENT)
    List<Target> getPhantomTargets(Object ingredient);

}

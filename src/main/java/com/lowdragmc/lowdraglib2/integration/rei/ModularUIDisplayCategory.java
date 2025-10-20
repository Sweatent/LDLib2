package com.lowdragmc.lowdraglib2.integration.rei;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date: 2022/11/27
 * @implNote ModularUIDisplayCategory
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Environment(EnvType.CLIENT)
public abstract class ModularUIDisplayCategory<T extends ModularDisplay<?>> implements DisplayCategory<T> {

    @Override
    public List<Widget> setupDisplay(T display, Rectangle bounds) {
        return display.createWidget(bounds);
    }
}

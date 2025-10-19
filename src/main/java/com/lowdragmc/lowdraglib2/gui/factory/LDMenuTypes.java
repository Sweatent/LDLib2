package com.lowdragmc.lowdraglib2.gui.factory;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIContainerMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class LDMenuTypes {
    public static final ExtendedScreenHandlerType<ModularUIContainerMenu> PLAYER_UI =
            new ExtendedScreenHandlerType<>(PlayerUIMenuType::create);

    private LDMenuTypes() {
    }

    public static void init() {
        Registry.register(Registries.MENU, LDLib2.id("player_ui"), PLAYER_UI);
    }
}

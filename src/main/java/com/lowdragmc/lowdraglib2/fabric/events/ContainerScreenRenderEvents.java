package com.lowdragmc.lowdraglib2.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Lightweight render callbacks for container screens used on the Fabric side.
 */
public final class ContainerScreenRenderEvents {
    private ContainerScreenRenderEvents() {
    }

    public static final Event<ScreenBackgroundRender> SCREEN_BACKGROUND = EventFactory.createArrayBacked(ScreenBackgroundRender.class, listeners -> (screen, graphics, mouseX, mouseY, delta) -> {
        for (ScreenBackgroundRender listener : listeners) {
            listener.onRender(screen, graphics, mouseX, mouseY, delta);
        }
    });

    public static final Event<ContainerRender> CONTAINER_BACKGROUND = EventFactory.createArrayBacked(ContainerRender.class, listeners -> (screen, graphics, mouseX, mouseY, delta) -> {
        for (ContainerRender listener : listeners) {
            listener.onRender(screen, graphics, mouseX, mouseY, delta);
        }
    });

    public static final Event<ContainerRender> CONTAINER_FOREGROUND = EventFactory.createArrayBacked(ContainerRender.class, listeners -> (screen, graphics, mouseX, mouseY, delta) -> {
        for (ContainerRender listener : listeners) {
            listener.onRender(screen, graphics, mouseX, mouseY, delta);
        }
    });

    @FunctionalInterface
    public interface ScreenBackgroundRender {
        void onRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float delta);
    }

    @FunctionalInterface
    public interface ContainerRender {
        void onRender(AbstractContainerScreen<?> screen, GuiGraphics graphics, int mouseX, int mouseY, float delta);
    }
}

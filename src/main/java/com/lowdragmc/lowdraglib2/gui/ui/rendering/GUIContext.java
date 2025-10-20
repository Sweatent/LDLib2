package com.lowdragmc.lowdraglib2.gui.ui.rendering;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public class GUIContext {
    @Environment(EnvType.CLIENT)
    public ModularUI modularUI;
    @Environment(EnvType.CLIENT)
    public GuiGraphics graphics;
    @Environment(EnvType.CLIENT)
    public int mouseX, mouseY;
    @Environment(EnvType.CLIENT)
    public float partialTick;
    @Environment(EnvType.CLIENT)
    public PoseStack pose;

    @Environment(EnvType.CLIENT)
    public void drawTexture(IGuiTexture texture, float x, float y, float width, float height) {
        texture.draw(graphics, mouseX, mouseY, x, y, width, height, partialTick);
    }
}

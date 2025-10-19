package com.lowdragmc.lowdraglib2.client;

import com.lowdragmc.lowdraglib2.CommonProxy;
import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.client.renderer.ATESRRendererProvider;
import com.lowdragmc.lowdraglib2.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib2.client.shader.LDLibShaders;
import com.lowdragmc.lowdraglib2.client.utils.WidgetClientTooltipComponent;
import com.lowdragmc.lowdraglib2.editor.resource.PackResourceProvider;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.gui.util.WidgetTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceType;

@Environment(EnvType.CLIENT)
public final class ClientProxy {

    private ClientProxy() {
    }

    public static void init() {
        registerScreens();
        registerTooltipComponents();
        registerBlockEntityRenderers();
        registerLifecycleHooks();
        registerClientReloadListeners();
        registerAdditionalModels();
    }

    private static void registerScreens() {
        MenuScreens.register(LDMenuTypes.PLAYER_UI, ModularUIContainerScreen::new);
    }

    private static void registerTooltipComponents() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof WidgetTooltipComponent widgetTooltip) {
                return new WidgetClientTooltipComponent(widgetTooltip);
            }
            return null;
        });
    }

    private static void registerBlockEntityRenderers() {
        if (Platform.isDevEnv() && CommonProxy.TEST_BE_TYPE != null) {
            BlockEntityRenderers.register(CommonProxy.TEST_BE_TYPE, ATESRRendererProvider::new);
        }
        BlockEntityRenderers.register(CommonProxy.RENDERER_BE_TYPE, ATESRRendererProvider::new);
    }

    private static void registerLifecycleHooks() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            LDLibShaders.init();
            DrawerHelper.init();
        });
    }

    private static void registerClientReloadListeners() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(PackResourceProvider.Manager.INSTANCE);
    }

    private static void registerAdditionalModels() {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        for (var entry : resourceManager.listResources("models",
                id -> id.getNamespace().equals(LDLib2.MOD_ID) && id.getPath().endsWith(".json")).entrySet()) {
            if (entry.getValue().sourcePackId().equals(LDLib2.MOD_ID)) {
                ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath().replace("models/", "").replace(".json", ""));
                Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelLocation));
            }
        }
        for (IRenderer renderer : IRenderer.EVENT_REGISTERS) {
            renderer.onAdditionalModel(modelLocation -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelLocation)));
        }
    }
}

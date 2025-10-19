package com.lowdragmc.lowdraglib2;

import com.lowdragmc.lowdraglib2.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib2.client.renderer.block.RendererBlockEntity;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.factory_outdated.*;
import com.lowdragmc.lowdraglib2.integration.kjs.ui.BlockUIJSFactory;
import com.lowdragmc.lowdraglib2.integration.kjs.ui.ItemUIJSFactory;
import com.lowdragmc.lowdraglib2.networking.LDLNetworking;
import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.test.NoRendererTestBlock;
import com.lowdragmc.lowdraglib2.test.TestBlock;
import com.lowdragmc.lowdraglib2.test.TestBlockEntity;
import com.lowdragmc.lowdraglib2.test.TestItem;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CommonProxy {

    public static BlockEntityType<TestBlockEntity> TEST_BE_TYPE;
    public static BlockEntityType<RendererBlockEntity> RENDERER_BE_TYPE;

    private CommonProxy() {
    }

    public static void init() {
        registerBlocks();
        registerItems();
        registerBlockEntities();
        LDMenuTypes.init();
        initializeSystems();
        registerPlugins();
        LDLNetworking.init();
    }

    private static void registerBlocks() {
        if (Platform.isDevEnv()) {
            Registry.register(Registries.BLOCK, LDLib2.id("test"), TestBlock.BLOCK);
            Registry.register(Registries.BLOCK, LDLib2.id("test_2"), NoRendererTestBlock.BLOCK);
        }
        Registry.register(Registries.BLOCK, LDLib2.id("renderer_block"), RendererBlock.BLOCK);
    }

    private static void registerItems() {
        if (Platform.isDevEnv()) {
            Registry.register(Registries.ITEM, LDLib2.id("test"), TestItem.ITEM);
            Registry.register(Registries.ITEM, LDLib2.id("test_2"), new BlockItem(NoRendererTestBlock.BLOCK, new Item.Properties()));
        }
    }

    private static void registerBlockEntities() {
        if (Platform.isDevEnv()) {
            TEST_BE_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, LDLib2.id("test"),
                    FabricBlockEntityTypeBuilder.create(TestBlockEntity::new, TestBlock.BLOCK).build());
        }
        RENDERER_BE_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, LDLib2.id("renderer_block"),
                FabricBlockEntityTypeBuilder.create(RendererBlockEntity::new, RendererBlock.BLOCK).build());
    }

    private static void registerPlugins() {
        FabricLoader.getInstance().getEntrypoints("ldlib2:plugin", ILDLibPlugin.class).forEach(plugin -> {
            try {
                plugin.onLoad();
            } catch (Throwable throwable) {
                LDLib2.LOGGER.error("Failed to load plugin {}", plugin.getClass().getName(), throwable);
            }
        });
    }

    private static void initializeSystems() {
        UIFactory.register(BlockEntityUIFactory.INSTANCE);
        UIFactory.register(HeldItemUIFactory.INSTANCE);
        if (LDLib2.isKubejsLoaded()) {
            UIFactory.register(BlockUIJSFactory.INSTANCE);
            UIFactory.register(ItemUIJSFactory.INSTANCE);
        }
        AccessorRegistries.init();
        LDLib2Registries.init();
    }

}

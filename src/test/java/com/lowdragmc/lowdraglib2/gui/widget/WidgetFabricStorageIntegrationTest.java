package com.lowdragmc.lowdraglib2.gui.widget;

import com.lowdragmc.lowdraglib2.misc.FluidStorage;
import com.lowdragmc.lowdraglib2.misc.ItemStackTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WidgetFabricStorageIntegrationTest {

    @Test
    void tankWidgetReflectsStorageState() {
        FluidStorage storage = new FluidStorage(1000);
        storage.fill(new FluidStack(Fluids.WATER, 750), IFluidHandler.FluidAction.EXECUTE);

        TankWidget widget = new TankWidget(storage, 0, 0, 18, 18, true, true);
        widget.initTemplate();
        widget.setOnAddedTooltips((w, tips) -> tips.add(Component.literal("extra")));
        widget.setClientSideWidget();

        assertSame(TankWidget.FLUID_SLOT_TEXTURE, widget.getBackgroundTexture(), "TankWidget should retain default texture");
        List<Component> supplemental = widget.getTooltipTexts();
        assertTrue(supplemental.stream().anyMatch(c -> c.getString().equals("extra")), "Custom tooltip should be appended");

        List<Component> fullTips = widget.getFullTooltipTexts();
        assertFalse(fullTips.isEmpty(), "Full tooltip list should not be empty");
        assertTrue(fullTips.stream().anyMatch(c -> c.getString().contains("750")), "Amount tooltip should reflect stored fluid");
    }

    @Test
    void slotWidgetUsesItemTransferStorage() {
        ItemStackTransfer transfer = new ItemStackTransfer(1);
        transfer.setStackInSlot(0, new ItemStack(Items.APPLE, 12), true);

        SlotWidget widget = new SlotWidget(transfer, 0, 0, 0, false, false);
        widget.initTemplate();
        widget.setOnAddedTooltips((w, tips) -> tips.add(Component.literal("apples")));
        widget.setClientSideWidget();

        assertSame(SlotWidget.ITEM_SLOT_TEXTURE, widget.getBackgroundTexture(), "SlotWidget should retain default texture");
        assertEquals(12, widget.getItem().getCount(), "Widget should expose inserted stack count");

        List<Component> tips = widget.getTooltipTexts();
        assertTrue(tips.stream().anyMatch(c -> c.getString().equals("apples")), "Custom tooltip should be included");
    }
}

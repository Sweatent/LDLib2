package com.lowdragmc.lowdraglib2.gui.widget;

import com.lowdragmc.lowdraglib2.misc.FabricFluidTransfer;
import com.lowdragmc.lowdraglib2.misc.FabricItemTransfer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WidgetStorageIntegrationTest {

    @Test
    void tankWidgetReflectsFabricStorageStateInTooltips() {
        FabricFluidTransfer storage = new FabricFluidTransfer(2000);
        storage.setFluidInTank(0, new FluidStack(Fluids.WATER, 750));

        TankWidget widget = new TankWidget(storage, 0, 0, 0, 18, 18, true, true);
        widget.setClientSideWidget();
        widget.detectAndSendChanges();

        List<Component> tooltips = widget.getFullTooltipTexts();
        assertFalse(tooltips.isEmpty(), "Tooltip list should contain entries");
        String tooltipText = tooltips.get(0).getString().toLowerCase();
        assertTrue(tooltipText.contains("water"), () -> "Expected water name in tooltip but got " + tooltipText);
        assertTrue(tooltips.stream().anyMatch(component -> component.getString().contains("750")),
                "Tooltip should contain current amount");
    }

    @Test
    void slotWidgetUsesFabricItemTransferForStateAndTooltips() {
        FabricItemTransfer items = new FabricItemTransfer(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.APPLE, 8)));

        SlotWidget widget = new SlotWidget(items, 0, 0, 0, true, true);
        widget.setOnAddedTooltips((slot, list) -> list.add(Component.literal("extra tooltip")));
        widget.detectAndSendChanges();

        assertEquals(8, widget.getItem().getCount());
        List<Component> tooltips = widget.getTooltipTexts();
        assertTrue(tooltips.stream().anyMatch(component -> component.getString().equals("extra tooltip")));
        assertNotNull(widget.getBackgroundTexture());
    }
}

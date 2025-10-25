package com.lowdragmc.lowdraglib2.graphprocessor.nodes.minecraft.item;

import com.lowdragmc.lowdraglib2.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib2.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib2.graphprocessor.data.trigger.LinearTriggerNode;
import com.lowdragmc.lowdraglib2.utils.Vector3fHelper;
import com.lowdragmc.lowdraglib2.misc.ItemTransfer;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.joml.Vector3f;

@LDLRegister(name = "item transfer get", group = "graph_processor.node.minecraft.item", registry = "ldlib2:graph_node")
public class ItemTransferGetNode extends LinearTriggerNode {
    @InputPort
    public Level level;
    @InputPort
    public Vector3f xyz;
    @InputPort(name = "direction")
    public Direction direction;
    @OutputPort(name = "item transfer")
    public ItemTransfer itemTransfer;

    @Override
    public void process() {
        if (level != null && xyz != null) {
            var pos = Vector3fHelper.toBlockPos(xyz);
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
            itemTransfer = handler == null ? null : ItemTransfer.of(handler);
        }
    }
}

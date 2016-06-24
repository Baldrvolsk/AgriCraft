package com.infinityraider.agricraft.compat.computercraft.method;

import com.infinityraider.agricraft.container.ContainerSeedAnalyzer;
import com.infinityraider.agricraft.tiles.peripheral.TileEntityPeripheral;
import net.minecraft.item.ItemStack;

public class MethodGetSpecimen extends MethodBasePeripheral {
    public MethodGetSpecimen() {
        super("getSpecimen");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws MethodException {
        ItemStack stack = peripheral.getStackInSlot(ContainerSeedAnalyzer.seedSlotId);
        return new Object[] {stack==null?null:stack.getDisplayName()};
    }
}
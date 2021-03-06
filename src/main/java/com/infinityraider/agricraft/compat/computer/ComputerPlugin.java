/*
 */
package com.infinityraider.agricraft.compat.computer;

import com.agricraft.agricore.core.AgriCore;
import com.agricraft.agricore.util.TypeHelper;
import com.infinityraider.agricraft.api.v1.plugin.AgriPlugin;
import com.infinityraider.agricraft.api.v1.IAgriPlugin;
import com.infinityraider.agricraft.compat.computer.blocks.BlockPeripheral;
import com.infinityraider.agricraft.compat.computer.tiles.TileEntityPeripheral;
import com.infinityraider.infinitylib.render.block.BlockRendererRegistry;
import java.util.Set;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 *
 */
@AgriPlugin
public class ComputerPlugin implements IAgriPlugin {

    public static final BlockPeripheral PERHIPHERAL = new BlockPeripheral();
    public static final Set<String> COMPUTER_MODS = TypeHelper.asSet(
            "computercraft",
            "OpenComputers"
    );
    public static final boolean ENABLED = COMPUTER_MODS.stream().anyMatch(Loader::isModLoaded);

    @Override
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public void initPlugin() {
        //RegisterHelper.registerBlock(PERHIPHERAL, Reference.MOD_ID.toLowerCase(), PERHIPHERAL.getInternalName());
        GameRegistry.registerTileEntity(TileEntityPeripheral.class, PERHIPHERAL.getInternalName());
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            BlockRendererRegistry.getInstance().registerCustomBlockRenderer(PERHIPHERAL);
        }
        AgriCore.getLogger("Computer Integration Enabled!");
    }

}

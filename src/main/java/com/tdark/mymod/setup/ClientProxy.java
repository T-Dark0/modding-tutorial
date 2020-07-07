package com.tdark.mymod.setup;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.blocks.screens.FirstBlockScreen;
import com.tdark.mymod.blocks.screens.ScreenPowerCharger;
import com.tdark.mymod.entities.ModEntities;
import com.tdark.mymod.entities.WeirdEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy implements IProxy {
    @Override
    public void init() {

        //Containers
        ScreenManager.registerFactory(ModBlocks.FIRSTBLOCK_CONTAINER, FirstBlockScreen::new);
        ScreenManager.registerFactory(ModBlocks.POWERCHARGER_CONTAINER, ScreenPowerCharger::new);

        //Entity Renderers
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.WEIRDENTITY, WeirdEntityRenderer::new);
    }

    @Override
    public World getClientWorld() {
        //Take a look at the import. Minecraft is a client-only class
        return Minecraft.getInstance().world;
    }
}

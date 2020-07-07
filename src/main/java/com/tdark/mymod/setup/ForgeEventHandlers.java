package com.tdark.mymod.setup;

import com.tdark.mymod.commands.ModCommands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onServerLoad(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }
}

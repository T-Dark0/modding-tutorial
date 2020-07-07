package com.tdark.mymod.setup;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.networking.Networking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ModSetup
{
    public ItemGroup itemGroup = new ItemGroup("mymod") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.FIRSTBLOCK);
        }
    };

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        Networking.registerMessages();
    }
}

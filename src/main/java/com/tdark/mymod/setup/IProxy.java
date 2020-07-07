package com.tdark.mymod.setup;

import net.minecraft.world.World;

public interface IProxy
{
    void init();
    World getClientWorld();
}

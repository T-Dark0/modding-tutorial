package com.tdark.mymod.setup;

import net.minecraft.world.World;

public class ServerProxy implements IProxy
{
    @Override
    public void init()
    {

    }

    @Override
    public World getClientWorld()
    {
        throw new IllegalStateException("The server doesn't have a client world");
    }
}

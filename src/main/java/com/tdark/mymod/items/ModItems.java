package com.tdark.mymod.items;

import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("mymod")
public class ModItems
{
    @ObjectHolder("firstitem")
    public static FirstItem FIRSTITEM;

    @ObjectHolder("weirdmob_egg")
    public static WeirdEntityEggItem WEIRDMOB_EGG;

    @ObjectHolder("teleport_staff")
    public static ItemTeleportStaff TELEPORT_STAFF;
}

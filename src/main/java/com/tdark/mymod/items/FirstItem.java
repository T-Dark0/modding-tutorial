package com.tdark.mymod.items;

import com.tdark.mymod.MyMod;
import net.minecraft.item.Item;

public class FirstItem extends Item
{
    public FirstItem()
    {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(MyMod.setup.itemGroup));

        setRegistryName("firstitem");
    }
}

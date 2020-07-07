package com.tdark.mymod.entities;

import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("mymod")
public class ModEntities {

    @ObjectHolder("weirdentity")
    public static EntityType<WeirdEntity> WEIRDENTITY;
}

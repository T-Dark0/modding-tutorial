package com.tdark.mymod.blocks;

import com.tdark.mymod.blocks.containers.ContainerPowerCharger;
import com.tdark.mymod.blocks.containers.FirstBlockContainer;
import com.tdark.mymod.blocks.tileentities.FirstBlockTile;
import com.tdark.mymod.blocks.tileentities.TileEntityPowerCharger;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


@ObjectHolder("mymod")
public class ModBlocks
{

    //First Block
    @ObjectHolder("firstblock")
    public static FirstBlock FIRSTBLOCK;

    @ObjectHolder("firstblock")
    public static TileEntityType<FirstBlockTile> FIRSTBLOCK_TILE;

    @ObjectHolder("firstblock")
    public static ContainerType<FirstBlockContainer> FIRSTBLOCK_CONTAINER;

    //Fancy Block
    @ObjectHolder("fancyblock")
    public static FancyBlock FANCYBLOCK;

    @ObjectHolder("fancyblock")
    public static TileEntityType<FancyBlockTile> FANCYBLOCK_TILE;

    //Power Charger
    @ObjectHolder("block_power_charger")
    public static final BlockPowerCharger POWERCHARGER = null;

    @ObjectHolder("block_power_charger")
    public static final TileEntityType<TileEntityPowerCharger> POWERCHARGER_TILE = null;

    @ObjectHolder("block_power_charger")
    public static final ContainerType<ContainerPowerCharger> POWERCHARGER_CONTAINER = null;
 }

package com.tdark.mymod.datagen;

import com.tdark.mymod.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {
    public LootTables(DataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModBlocks.FIRSTBLOCK, createStandardTable("firstblock", ModBlocks.FIRSTBLOCK));
    }
}

package com.tdark.mymod.datagen;

import com.tdark.mymod.blocks.ModBlocks;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(ModBlocks.FIRSTBLOCK)
                .patternLine("ccc")
                .patternLine("crc")
                .patternLine("ccc")
                .key('c', Blocks.COBBLESTONE)
                .key('r', Tags.Items.DYES_RED)
                .setGroup("My Mod")
                .addCriterion("cobblestone", InventoryChangeTrigger.Instance.forItems(Blocks.COBBLESTONE)) //used for the recipe book
                .build(consumer);

    }
}

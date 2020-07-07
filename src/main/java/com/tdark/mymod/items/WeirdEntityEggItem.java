package com.tdark.mymod.items;

import com.tdark.mymod.MyMod;
import com.tdark.mymod.entities.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.Objects;

public class WeirdEntityEggItem extends Item {

    public WeirdEntityEggItem() {
        super(new Item.Properties()
            .group(MyMod.setup.itemGroup)
        );
        setRegistryName("weirdmob_egg");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        if(world.isRemote) {
            return ActionResultType.SUCCESS;

        } else {
            ItemStack eggItemStack = context.getItem();
            BlockPos hitBlockPos = context.getPos();
            Direction hitBlockFace = context.getFace();
            BlockState hitBlockState = world.getBlockState(hitBlockPos);
            Block hitBlock = hitBlockState.getBlock();

            if(hitBlock == Blocks.SPAWNER) {
                TileEntity spawnerTE = world.getTileEntity((hitBlockPos));
                if(spawnerTE instanceof MobSpawnerTileEntity) {
                    AbstractSpawner abstractSpawner = ((MobSpawnerTileEntity) spawnerTE).getSpawnerBaseLogic();
                    abstractSpawner.setEntityType(ModEntities.WEIRDENTITY);
                    spawnerTE.markDirty();
                    world.notifyBlockUpdate(hitBlockPos, hitBlockState, hitBlockState, 0b11);
                    eggItemStack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }

            BlockPos spawningPos;
            if(hitBlockState.getCollisionShape(world, hitBlockPos).isEmpty()) {
                spawningPos = hitBlockPos;
            } else {
                spawningPos = hitBlockPos.offset(hitBlockFace);
            }

            Entity spawnedEntity = ModEntities.WEIRDENTITY.spawn(
                    world,
                    eggItemStack,
                    context.getPlayer(),
                    hitBlockPos,
                    SpawnReason.SPAWN_EGG,
                    true,
                    !Objects.equals(hitBlockPos, spawningPos) && hitBlockFace == Direction.UP);
            if(spawnedEntity != null) {
                eggItemStack.shrink(1);
            }
        }

        return ActionResultType.SUCCESS;
    }
}



package com.tdark.mymod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FancyBlock extends Block {
    private VoxelShape shape = VoxelShapes.create(.2, .2, .2, .8, .8, .8);
    //The default shape is 0, 0, 0, 1, 1, 1
    //shapes can also be combined

    public FancyBlock() {
        super(Properties.create(Material.ROCK)
            .sound(SoundType.GROUND)
            .hardnessAndResistance(2.0f)
        );
        setRegistryName("fancyblock");
    }

    @Override
    //getRenderShape and getCollisionShape also exist
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FancyBlockTile();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        ItemStack item = player.getHeldItem(hand);
        if (!item.isEmpty() && item.getItem() instanceof BlockItem && !world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof FancyBlockTile) {
                BlockState mimicState = ((BlockItem) item.getItem()).getBlock().getDefaultState();
                ((FancyBlockTile) te).setMimic(mimicState);
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, world, pos, player, hand, result);
    }
}

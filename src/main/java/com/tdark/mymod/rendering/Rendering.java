package com.tdark.mymod.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class Rendering {

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        if(event.getPhase() != EventPriority.NORMAL)
            return;

        // Get instances of the classes required for a block render.
        MinecraftServer server = Minecraft.getInstance().getIntegratedServer();
        World world = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, true);
        MatrixStack matrixStack = event.getMatrixStack();

        // Get the projected view coordinates.
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView(); //Camera position

        // Choose obsidian as the arbitrary block.
        BlockState blockState = Blocks.OBSIDIAN.getDefaultState();

        // Begin rendering the block.
        IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

        renderBlock(
                matrixStack,
                renderTypeBuffer,
                world,
                blockState,
                new BlockPos(0, 128, 0),
                projectedView,
                new Vec3d(0.0, 128.0, 0.0)
        );

        renderTypeBuffer.finish();
    }

    public static void renderBlock(
            MatrixStack matrixStack,
            IRenderTypeBuffer.Impl renderTypeBuffer,
            World world,
            BlockState blockState,
            BlockPos logicPos,
            Vec3d projectedView,
            Vec3d renderCoordinates
    )
    {
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        int overlay = OverlayTexture.NO_OVERLAY;

        matrixStack.push();
        matrixStack.translate(
                -projectedView.x + renderCoordinates.x, //move back to 0 from the camera, then move to the render coordinate
                -projectedView.y + renderCoordinates.y,
                -projectedView.z + renderCoordinates.z
        );

        for(RenderType renderType : RenderType.getBlockRenderTypes())
        {
            if(RenderTypeLookup.canRenderInLayer(blockState, renderType))
                blockRendererDispatcher.getBlockModelRenderer().renderModel(
                        world,
                        blockRendererDispatcher.getModelForState(blockState),
                        blockState,
                        logicPos,
                        matrixStack,
                        renderTypeBuffer.getBuffer(renderType),
                        true,
                        new Random(),
                        blockState.getPositionRandom(logicPos),
                        overlay
                );
        }

        matrixStack.pop();
    }
}

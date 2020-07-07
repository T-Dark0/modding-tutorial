package com.tdark.mymod.blocks;

import com.google.common.collect.ImmutableList;
import com.tdark.mymod.MyMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FancyBlockBakedModel
implements IDynamicBakedModel {

    public static final ResourceLocation TEXTURE = new ResourceLocation(MyMod.MODID, "block/fancyblock");

    private TextureAtlasSprite getTexture() {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE);
    }

    //a builder for a quadrilateral, the normal to the quad, the 3 coords of the vertex to put, the 2 texture map coords
    //The sprite, aka an abstraction to get our texture from the texture atlas (all textures in a big image, because the gpu
    //likes that better, and finally the RGB colors
    private void putVertex(BakedQuadBuilder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for(int i = 0; i<elements.size(); i++) {
            VertexFormatElement element = elements.get(i);
            switch (element.getUsage()) {
                case POSITION:
                    builder.put(i, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(i, r, g, b, 1.0f);
                    break;
                case UV:
                    switch (element.getIndex()) {
                        case 0:
                            float iu = sprite.getInterpolatedU(u);
                            float iv = sprite.getInterpolatedV(v);
                            builder.put(i, iu, iv);
                            break;
                        case 2:
                            builder.put(i, (short) 0, (short) 0);
                            break;
                        default:
                            builder.put(i);
                            break;
                    }
                    break;
                case NORMAL:
                    builder.put(i, (float)normal.x, (float)normal.y, (float)normal.z);
                    break;
                default:
                    builder.put(i);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        //(v3 - v2) x (v1 - v2). va - vb is the vector ba, and the cross product of two vectors is perpendicular to both
        //Hence, this is calculating two vectors (3->2 and 1->2), then getting a vector perpendicular to the plane they define
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();
        int tw = sprite.getWidth();
        int th = sprite.getHeight();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, th, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, tw, th, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, tw, 0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private static Vec3d v(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }

    @Nonnull
    @Override
    //Returns the quads that make up our model, based on state, side, the world's RNG, and some extra data
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        RenderType layer = MinecraftForgeClient.getRenderLayer();

        BlockState mimic = extraData.getData(FancyBlockTile.MIMIC);
        if (mimic != null && !(mimic.getBlock() instanceof FancyBlock)) {
            if (layer == null || RenderTypeLookup.canRenderInLayer(mimic, layer)) {
                IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(mimic);
                try {
                    return model.getQuads(mimic, side, rand, EmptyModelData.INSTANCE);
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            }
            return Collections.emptyList();
        }

        if (side != null || (layer != null && !layer.equals(RenderType.getSolid()))) {
            return Collections.emptyList();
        }

        TextureAtlasSprite texture = getTexture();
        List<BakedQuad> quads = new ArrayList<>();
        double l = .2;
        double r = 1-.2;
        quads.add(createQuad(v(l, r, l), v(l, r, r), v(r, r, r), v(r, r, l), texture));
        quads.add(createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), texture));
        quads.add(createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), texture));
        quads.add(createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), texture));
        quads.add(createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), texture));
        quads.add(createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), texture));

        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }
}

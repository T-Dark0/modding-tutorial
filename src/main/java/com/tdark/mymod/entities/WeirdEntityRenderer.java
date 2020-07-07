package com.tdark.mymod.entities;

import com.tdark.mymod.MyMod;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class WeirdEntityRenderer extends MobRenderer<WeirdEntity, WeirdEntityModel> {

    private static final ResourceLocation TEXTURE =  new ResourceLocation(MyMod.MODID, "textures/entity/weirdentity.png");

    public WeirdEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new WeirdEntityModel(), 0.5f);
    }

    @Override
    public ResourceLocation getEntityTexture(WeirdEntity entity) {
        return TEXTURE;
    }
}

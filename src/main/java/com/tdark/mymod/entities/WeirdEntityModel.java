package com.tdark.mymod.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class WeirdEntityModel extends EntityModel<WeirdEntity> {

    public ModelRenderer body;

    public WeirdEntityModel() {
        body = new ModelRenderer(this, 0, 0);
        body.addBox(-3, -3, -3, 6, 6, 6); //offset, then size (per axis)
    }

    @Override
    public void setRotationAngles(WeirdEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }


    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

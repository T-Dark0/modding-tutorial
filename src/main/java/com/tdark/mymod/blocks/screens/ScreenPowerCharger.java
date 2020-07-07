package com.tdark.mymod.blocks.screens;

import com.tdark.mymod.MyMod;
import com.tdark.mymod.blocks.containers.ContainerPowerCharger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ScreenPowerCharger extends ContainerScreen<ContainerPowerCharger> {

    private static final int HORIZONTAL_MARGIN = 8;
    private static final int VERTICAL_MARGIN = 6;

    private static final ResourceLocation GUI = new ResourceLocation(MyMod.MODID, "textures/gui/power_charger_gui.png");

    public ScreenPowerCharger(ContainerPowerCharger screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String energy = "Energy: " + container.getEnergy();
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        drawString(
                fontRenderer,
                energy,
                this.xSize - fontRenderer.getStringWidth(energy) - HORIZONTAL_MARGIN,
                VERTICAL_MARGIN,
                0xffffff);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }
}

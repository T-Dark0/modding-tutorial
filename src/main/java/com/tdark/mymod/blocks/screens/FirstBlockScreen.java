package com.tdark.mymod.blocks.screens;

import com.tdark.mymod.MyMod;
import com.tdark.mymod.blocks.containers.FirstBlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class FirstBlockScreen extends ContainerScreen<FirstBlockContainer> {

    private ResourceLocation GUI = new ResourceLocation(MyMod.MODID, "textures/gui/firstblock_gui.png");

    public FirstBlockScreen(FirstBlockContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getFormattedText(), 8.0f, 6.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, (float)(this.ySize - 96 - 2), 0x404040);

        String energy = "Energy" + container.getEnergy();
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        drawString(fontRenderer, energy, this.xSize - fontRenderer.getStringWidth(energy) - 8, 6, 0xffffff);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }
}

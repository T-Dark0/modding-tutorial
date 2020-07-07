package com.tdark.mymod.gui;

import com.tdark.mymod.MyMod;
import com.tdark.mymod.networking.Networking;
import com.tdark.mymod.networking.PacketSpawn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class SpawnerScreen extends Screen {

    private static final int WIDTH = 180;
    private static final int HEIGHT = 150;

    private static final ResourceLocation GUI = new ResourceLocation(MyMod.MODID, "textures/gui/spawner_gui.png");

    protected SpawnerScreen() {
        super(new StringTextComponent("Spawn Something"));
    }

    private void spawn(String id) {
        Networking.sendToServer(new PacketSpawn(id, minecraft.player.dimension, minecraft.player.getPosition()));
        minecraft.displayGuiScreen(null);
    }

    public static void open() {
        Minecraft.getInstance().displayGuiScreen(new SpawnerScreen());
    }


    @Override
    public void init() {
        int relX = (this.width - WIDTH) / 2;    //left of the window
        int relY = (this.height - HEIGHT) / 2;  //top of the window

        addButton(new Button(relX + 10, relY + 10, 160, 20, "Cow", button -> spawn("minecraft:cow")));
        addButton(new Button(relX + 10, relY + 37, 160, 20, "Pig", button -> spawn("minecraft:pig")));
        addButton(new Button(relX + 10, relY + 64, 160, 20, "Chicken", button -> spawn("minecraft:chicken")));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(relX, relY, 0, 0, WIDTH, HEIGHT);
        super.render(mouseX, mouseY, partialTicks);
    }
}

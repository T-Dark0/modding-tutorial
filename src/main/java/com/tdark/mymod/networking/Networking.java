package com.tdark.mymod.networking;

import com.tdark.mymod.MyMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    public static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MyMod.MODID, "mymod"), () -> "1.0", s -> true, s -> true);

        //Message to the client, to open a GUI
        INSTANCE.registerMessage(
                nextID(),               //Mod-level unique id of the message
                PacketOpenGui.class,    //Class of the message payload
                PacketOpenGui::toBytes, //Encoder, responsible for turning the message into bytes
                PacketOpenGui::new,     //Decoder, responsible for turning the bytes into a message
                PacketOpenGui::handle   //Handle, responsible for handling the message on the other side
        );

        //Message back to the server, to actually spawn the selected entity
        INSTANCE.registerMessage(
                nextID(),
                PacketSpawn.class,
                PacketSpawn::toBytes,
                PacketSpawn::new,
                PacketSpawn::handle
        );
    }

    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        INSTANCE.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}

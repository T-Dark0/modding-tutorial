package com.tdark.mymod.networking;

import com.tdark.mymod.gui.SpawnerScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenGui {

    public PacketOpenGui() {

    }

    //packet has no information, so we quite literally need to send only the class and the handler. Everything else is empty
    public void toBytes(PacketBuffer buf) {

    }

    public PacketOpenGui(PacketBuffer buf) {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        //Networking happens in a different thread from rendering and stuff. Messages need to be executed thread-safely
        ctx.get().enqueueWork(SpawnerScreen::open);
        ctx.get().setPacketHandled(true);
    }
}

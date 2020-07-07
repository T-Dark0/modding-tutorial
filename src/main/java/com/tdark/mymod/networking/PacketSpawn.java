package com.tdark.mymod.networking;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PacketSpawn {

    private final String id;
    private final DimensionType dimType;
    private final BlockPos pos;

    public PacketSpawn(String id, DimensionType dimType, BlockPos pos) {
        this.id = id;
        this.dimType = dimType;
        this.pos = pos;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(id);
        buf.writeInt(dimType.getId());
        buf.writeBlockPos(pos);
    }

    public PacketSpawn(PacketBuffer buf) {
        id = buf.readString();
        dimType = DimensionType.getById(buf.readInt());
        pos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
       ctx.get().enqueueWork(() -> {
           ServerWorld spawnWorld = ctx.get().getSender().server.getWorld(dimType);
           EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
           if(entityType == null) {
               throw new IllegalStateException("Unknown Id:" + id);
           }
           entityType.spawn(spawnWorld, null, null, pos, SpawnReason.COMMAND, true, true);
       });

       ctx.get().setPacketHandled(true);
    }
}

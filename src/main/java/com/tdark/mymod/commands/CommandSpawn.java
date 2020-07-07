package com.tdark.mymod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tdark.mymod.networking.Networking;
import com.tdark.mymod.networking.PacketOpenGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandSpawn
implements Command<CommandSource> {

    private static final CommandSpawn CMD = new CommandSpawn();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("spawn")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        Networking.sendToClient(new PacketOpenGui(), player);
        return 0;
    }
}

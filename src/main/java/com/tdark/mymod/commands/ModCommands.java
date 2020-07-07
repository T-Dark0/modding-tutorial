package com.tdark.mymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tdark.mymod.MyMod;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> myCmd = dispatcher.register(
                Commands.literal(MyMod.MODID)
                    .then(CommandTest.register(dispatcher))
                    .then(CommandSpawn.register(dispatcher))
        );

        dispatcher.register(Commands.literal("myCommand").redirect(myCmd));
    }
}

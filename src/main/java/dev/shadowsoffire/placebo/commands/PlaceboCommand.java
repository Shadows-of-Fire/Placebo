package dev.shadowsoffire.placebo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PlaceboCommand {

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext ctx) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("placebo");
        SerializeLootTableCommand.register(builder);
        HandToJsonCommand.register(builder);
        StringToObjCommand.register(builder, ctx);
        pDispatcher.register(builder);
    }

}

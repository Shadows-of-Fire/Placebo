package shadows.placebo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class PlaceboCommand {

	public static void register(CommandDispatcher<CommandSource> pDispatcher) {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("placebo");
		SerializeLootTableCommand.register(builder);
		pDispatcher.register(builder);
	}

}

package shadows.placebo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PlaceboCommand {

	public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("placebo");
		SerializeLootTableCommand.register(builder);
		pDispatcher.register(builder);
	}

}

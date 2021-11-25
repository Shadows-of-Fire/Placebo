package shadows.placebo.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.impl.LootCommand;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

public class SerializeLootTableCommand {

	public static final Gson GSON = LootSerializers.createLootTableSerializer().setPrettyPrinting().create();

	public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> {
		return new TranslationTextComponent("placebo.cmd.not_found", arg);
	});

	public static void register(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(Commands.literal("serialize_loot_table").then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(LootCommand.SUGGEST_LOOT_TABLE).executes(ctx -> {
			ResourceLocation id = ResourceLocationArgument.getId(ctx, "loot_table");
			LootTable table = ctx.getSource().getServer().getLootTables().get(id);
			if (table == LootTable.EMPTY) throw NOT_FOUND.create(id);
			String path = "placebo_serialized/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json";
			File file = new File(FMLPaths.GAMEDIR.get().toFile(), path);
			file.getParentFile().mkdirs();
			if (attemptSerialize(table, file)) {
				ctx.getSource().sendSuccess(new TranslationTextComponent("placebo.cmd.serialize_success", id, path), true);
			} else ctx.getSource().sendFailure(new TranslationTextComponent("placebo.cmd.serialize_failure"));
			return 0;
		})));
	}

	public static boolean attemptSerialize(LootTable table, File file) {
		String json = GSON.toJson(table);
		try (FileWriter w = new FileWriter(file)) {
			w.write(json);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

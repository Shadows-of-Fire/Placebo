package dev.shadowsoffire.placebo.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.loading.FMLPaths;

public class SerializeLootTableCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("serialize_loot_table").requires(s -> s.hasPermission(2)).then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(LootCommand.SUGGEST_LOOT_TABLE).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "loot_table");
            LootTable table = ctx.getSource().getServer().getServerResources().managers().getLootData().getLootTable(id);
            if (table == LootTable.EMPTY) throw NOT_FOUND.create(id);
            String path = "placebo_serialized/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json";
            File file = new File(FMLPaths.GAMEDIR.get().toFile(), path);
            file.getParentFile().mkdirs();
            if (attemptSerialize(table, file)) {
                ctx.getSource().sendSuccess(() -> Component.translatable("placebo.cmd.serialize_success", id, path), true);
            }
            else ctx.getSource().sendFailure(Component.translatable("placebo.cmd.serialize_failure"));
            return 0;
        })));
    }

    public static boolean attemptSerialize(LootTable table, File file) {
        JsonElement json = LootTable.CODEC.encodeStart(JsonOps.INSTANCE, table).get().left().get();
        try (FileWriter w = new FileWriter(file)) {
            w.write(GSON.toJson(json));
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

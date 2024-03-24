package dev.shadowsoffire.placebo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import dev.shadowsoffire.placebo.util.data.RuntimeDatagenHelpers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.storage.loot.LootTable;

public class SerializeLootTableCommand {

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("serialize_loot_table").requires(s -> s.hasPermission(2)).then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(LootCommand.SUGGEST_LOOT_TABLE).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "loot_table");
            LootTable table = ctx.getSource().getServer().getServerResources().managers().getLootData().getLootTable(id);
            if (table == LootTable.EMPTY) throw NOT_FOUND.create(id);
            if (attemptSerialize(table, id)) {
                String path = "datagen/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json";
                ctx.getSource().sendSuccess(() -> Component.translatable("placebo.cmd.serialize_success", id, path), true);
            }
            else ctx.getSource().sendFailure(Component.translatable("placebo.cmd.serialize_failure"));
            return 0;
        })));
    }

    public static boolean attemptSerialize(LootTable table, ResourceLocation id) {
        try {
            RuntimeDatagenHelpers.write(table, LootTable.CODEC, "loot_tables", id);
            return true;
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package dev.shadowsoffire.placebo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.util.data.RuntimeDatagenHelpers;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.LootTable;

public class SerializeLootTableCommand {

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext context) {
        builder.then(Commands.literal("serialize_loot_table").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(context)).executes(ctx -> {
            Holder<LootTable> table = ResourceOrIdArgument.getLootTable(ctx, "loot_table");
            Identifier id = table.unwrapKey().map(key -> key.identifier()).orElse(null);
            if (id == null || table.value() == LootTable.EMPTY) {
                throw NOT_FOUND.create(id);
            }

            var access = ctx.getSource().registryAccess();
            RuntimeDatagenHelpers.write(LootTable.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, access), table.value()).getOrThrow(), "loot_table", id);
            final Identifier finalId = id;
            String path = "datagen/" + finalId.getNamespace() + "/loot_table/" + finalId.getPath() + ".json";
            ctx.getSource().sendSuccess(() -> Component.translatable("placebo.cmd.serialize_success", finalId.toString(), path), true);
            return 0;
        })));
    }
}

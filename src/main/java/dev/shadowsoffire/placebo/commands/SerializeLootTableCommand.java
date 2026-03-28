package dev.shadowsoffire.placebo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.util.data.RuntimeDatagenHelpers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.storage.loot.LootTable;

public class SerializeLootTableCommand {

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("serialize_loot_table").requires(s -> s.hasPermission(2)).then(Commands.argument("loot_table", IdentifierArgument.id()).suggests(LootCommand.SUGGEST_LOOT_TABLE).executes(ctx -> {
            Identifier id = IdentifierArgument.getId(ctx, "loot_table");
            RegistryAccess access = ctx.getSource().getServer().reloadableRegistries().get();
            LootTable table = access.registryOrThrow(Registries.LOOT_TABLE).get(id);
            if (table == LootTable.EMPTY) {
                throw NOT_FOUND.create(id);
            }

            RuntimeDatagenHelpers.write(LootTable.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, access), table).getOrThrow(), "loot_table", id);
            String path = "datagen/" + id.getNamespace() + "/loot_table/" + id.getPath() + ".json";
            ctx.getSource().sendSuccess(() -> Component.translatable("placebo.cmd.serialize_success", id.toString(), path), true);
            return 0;
        })));
    }
}

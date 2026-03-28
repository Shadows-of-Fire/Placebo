package dev.shadowsoffire.placebo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

public class GetDimensionTypeCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("get_dimension_type").executes(ctx -> {
            ServerLevel level = ctx.getSource().getLevel();
            Registry<DimensionType> reg = level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
            DimensionType type = level.dimensionType();
            Identifier key = reg.getKey(type);
            ctx.getSource().sendSuccess(() -> Component.translatable("Dimension type for current level: %s", key.toString()), true);
            return 0;
        }));
    }

}

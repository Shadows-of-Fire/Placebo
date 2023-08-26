package dev.shadowsoffire.placebo.commands;

import java.io.IOException;
import java.io.StringWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Deserializers;

public class HandToJsonCommand {

    public static final Gson GSON = Deserializers.createLootTableSerializer().setPrettyPrinting().create();

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("hand").executes(ctx -> {
            try {
                String str = toJsonStr(ctx.getSource().getEntity().getHandSlots().iterator().next());
                ctx.getSource().sendSystemMessage(Component.literal(str));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }));
    }

    public static String toJsonStr(ItemStack stack) throws IOException {
        JsonElement json = ItemAdapter.CODEC.encodeStart(JsonOps.INSTANCE, stack).get().left().get();
        StringWriter str = new StringWriter();
        JsonWriter writer = GSON.newJsonWriter(str);
        writer.setIndent("    ");
        GSON.toJson(json, JsonObject.class, writer);
        return String.format("Currently Held Item:\n%s", str.toString());
    }
}

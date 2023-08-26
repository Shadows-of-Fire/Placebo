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

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.Deserializers;

public class StringToObjCommand {

    public static final Gson GSON = Deserializers.createLootTableSerializer().setPrettyPrinting().create();

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext buildCtx) {
        builder.then(Commands.literal("string_to_obj").requires(src -> src.hasPermission(2)).then(Commands.argument("nbt_item", ItemArgument.item(buildCtx)).executes(ctx -> {
            try {
                String str = toJsonStr(ItemArgument.getItem(ctx, "nbt_item").createItemStack(1, false).getTag());
                if (ctx.getSource().getPlayer() != null) {
                    ctx.getSource().sendSystemMessage(Component.literal(str));
                }
                else {
                    Placebo.LOGGER.info(str);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        })));
    }

    public static String toJsonStr(CompoundTag tag) throws IOException {
        JsonElement json = NBTAdapter.EITHER_CODEC.encodeStart(JsonOps.INSTANCE, tag).get().left().get();
        StringWriter str = new StringWriter();
        JsonWriter writer = GSON.newJsonWriter(str);
        writer.setIndent("    ");
        GSON.toJson(json, JsonObject.class, writer);
        return String.format("JSON Object Form:\n%s", str.toString());
    }
}

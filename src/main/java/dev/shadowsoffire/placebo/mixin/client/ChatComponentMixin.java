package dev.shadowsoffire.placebo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.shadowsoffire.placebo.commands.HandToJsonCommand;
import net.minecraft.client.gui.components.ChatComponent;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    /**
     * Causes newlines to be unescaped when logging chat, so that {@link HandToJsonCommand} can log properly.
     */
    @ModifyConstant(method = "logChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/GuiMessageTag;)V")
    public String placebo_unEscapeChatLogNewlines(String old) {
        if (old.equals("\\\\n")) return "\n";
        return old;
    }

}

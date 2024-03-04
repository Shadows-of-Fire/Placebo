package dev.shadowsoffire.placebo.packets;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Allows for easy implementations of client->server button presses. Sends an integer that allows for arbitrary data encoding schemes within the integer
 * space.<br>
 * The Container must implement {@link IButtonContainer}.<br>
 * Defer to using using {@link MultiPlayerGameMode#handleInventoryButtonClick} and {@link AbstractContainerMenu#clickMenuButton} when the buttonId can be a
 * byte.
 */
public class ButtonClickMessage implements CustomPacketPayload {

    public static final ResourceLocation ID = Placebo.loc("button_click");

    private int button;

    public ButtonClickMessage(int button) {
        this.button = button;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.button);
    }

    @Override
    public ResourceLocation id() {
        return null;
    }

    public static interface IButtonContainer {
        void onButtonClick(int id);
    }

    public static class Provider implements PayloadProvider<ButtonClickMessage, PlayPayloadContext> {

        @Override
        public ResourceLocation getMsgId() {
            return ID;
        }

        @Override
        public ButtonClickMessage read(FriendlyByteBuf buf) {
            return new ButtonClickMessage(buf.readInt());
        }

        @Override
        public void handle(ButtonClickMessage msg, PlayPayloadContext ctx) {
            PayloadHelper.handle(() -> {
                if (ctx.player().get().containerMenu instanceof IButtonContainer) {
                    ((IButtonContainer) ctx.player().get().containerMenu).onButtonClick(msg.button);
                }
            }, ctx);
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

    }

}

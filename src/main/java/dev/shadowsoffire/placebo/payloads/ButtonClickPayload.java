package dev.shadowsoffire.placebo.payloads;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Allows for easy implementations of client->server button presses. Sends an integer that allows for arbitrary data encoding schemes within the integer
 * space.<br>
 * The Container must implement {@link IButtonContainer}.<br>
 * Defer to using using {@link MultiPlayerGameMode#handleInventoryButtonClick} and {@link AbstractContainerMenu#clickMenuButton} when the buttonId can be a
 * byte.
 */
public record ButtonClickPayload(int button) implements CustomPacketPayload {

    public static final Type<ButtonClickPayload> TYPE = new Type<>(Placebo.loc("button_click"));

    public static final StreamCodec<FriendlyByteBuf, ButtonClickPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ButtonClickPayload::button,
        ButtonClickPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static interface IButtonContainer {
        void onButtonClick(int id);
    }

    public static class Provider implements PayloadProvider<ButtonClickPayload> {

        @Override
        public Type<ButtonClickPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ButtonClickPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleServer(ButtonClickPayload msg, IPayloadContext ctx) {
            if (ctx.player().containerMenu instanceof IButtonContainer) {
                ((IButtonContainer) ctx.player().containerMenu).onButtonClick(msg.button);
            }
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

        @Override
        public String getVersion() {
            return "1";
        }

    }

}

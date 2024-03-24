package dev.shadowsoffire.placebo.packets;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class PatreonDisableMessage implements CustomPacketPayload {

    public static final ResourceLocation ID = Placebo.loc("patreon_disable");

    private int type;
    private UUID id;

    public PatreonDisableMessage(int type) {
        this.type = type;
    }

    public PatreonDisableMessage(int type, UUID id) {
        this(type);
        this.id = id;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.type);
        buf.writeByte(this.id == null ? 0 : 1);
        if (this.id != null) buf.writeUUID(this.id);
    }

    public static class Provider implements PayloadProvider<PatreonDisableMessage, PlayPayloadContext> {

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public PatreonDisableMessage read(FriendlyByteBuf buf) {
            int type = buf.readByte();
            if (buf.readByte() == 1) {
                return new PatreonDisableMessage(type, buf.readUUID());
            }
            else return new PatreonDisableMessage(type);
        }

        @Override
        public void handle(PatreonDisableMessage msg, PlayPayloadContext ctx) {
            if (ctx.flow() == PacketFlow.SERVERBOUND) {
                PayloadHelper.handle(() -> {
                    PacketDistro.sendToAll(new PatreonDisableMessage(msg.type, ctx.player().get().getUUID()));
                }, ctx);
            }
            else PayloadHelper.handle(() -> {
                Set<UUID> set = msg.type == 0 ? TrailsManager.DISABLED : WingsManager.DISABLED;
                if (set.contains(msg.id)) {
                    set.remove(msg.id);
                }
                else set.add(msg.id);
            }, ctx);
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.empty();
        }

    }

}

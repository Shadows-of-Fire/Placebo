package shadows.placebo.packets;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import shadows.placebo.Placebo;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.PacketDistro;
import shadows.placebo.patreon.TrailsManager;
import shadows.placebo.patreon.WingsManager;

public class PatreonDisableMessage implements MessageProvider<PatreonDisableMessage> {

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
    public void write(PatreonDisableMessage msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.type);
        buf.writeByte(msg.id == null ? 0 : 1);
        if (msg.id != null) buf.writeUUID(msg.id);
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
    public void handle(PatreonDisableMessage msg, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            MessageHelper.handlePacket(() -> () -> {
                PacketDistro.sendToAll(Placebo.CHANNEL, new PatreonDisableMessage(msg.type, ctx.get().getSender().getUUID()));
            }, ctx);
        }
        else MessageHelper.handlePacket(() -> () -> {
            Set<UUID> set = msg.type == 0 ? TrailsManager.DISABLED : WingsManager.DISABLED;
            if (set.contains(msg.id)) {
                set.remove(msg.id);
            }
            else set.add(msg.id);
        }, ctx);
    }

}

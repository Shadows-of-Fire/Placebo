package shadows.placebo.net;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import shadows.placebo.Placebo;
import shadows.placebo.patreon.TrailsManager;
import shadows.placebo.patreon.WingsManager;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class MessagePatreonDisable extends MessageProvider<MessagePatreonDisable> {

	private int type;
	private UUID id;

	public MessagePatreonDisable(int type) {
		this.type = type;
	}

	public MessagePatreonDisable(int type, UUID id) {
		this(type);
		this.id = id;
	}

	@Override
	public void write(MessagePatreonDisable msg, PacketBuffer buf) {
		buf.writeByte(msg.type);
		buf.writeByte(msg.id == null ? 0 : 1);
		if (msg.id != null) buf.writeUUID(msg.id);
	}

	@Override
	public MessagePatreonDisable read(PacketBuffer buf) {
		int type = buf.readByte();
		if (buf.readByte() == 1) {
			return new MessagePatreonDisable(type, buf.readUUID());
		} else return new MessagePatreonDisable(type);
	}

	@Override
	public void handle(MessagePatreonDisable msg, Supplier<Context> ctx) {
		if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) NetworkUtils.handlePacket(() -> () -> {
			Placebo.CHANNEL.send(PacketDistributor.ALL.noArg(), new MessagePatreonDisable(msg.type, ctx.get().getSender().getUUID()));
		}, ctx.get());
		else NetworkUtils.handlePacket(() -> () -> {
			Set<UUID> set = msg.type == 0 ? TrailsManager.DISABLED : WingsManager.DISABLED;
			if (set.contains(msg.id)) {
				set.remove(msg.id);
			} else set.add(msg.id);
		}, ctx.get());
	}

}

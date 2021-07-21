package shadows.placebo.net;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class MessageButtonClick extends MessageProvider<MessageButtonClick> {

	int button;

	public MessageButtonClick(int button) {
		this.button = button;
	}

	public MessageButtonClick() {

	}

	@Override
	public Class<MessageButtonClick> getMsgClass() {
		return MessageButtonClick.class;
	}

	@Override
	public MessageButtonClick read(PacketBuffer buf) {
		return new MessageButtonClick(buf.readInt());
	}

	@Override
	public void write(MessageButtonClick msg, PacketBuffer buf) {
		buf.writeInt(msg.button);
	}

	@Override
	public void handle(MessageButtonClick msg, Supplier<Context> ctx) {
		NetworkUtils.handlePacket(() -> () -> {
			if (ctx.get().getSender().containerMenu instanceof IButtonContainer) {
				((IButtonContainer) ctx.get().getSender().containerMenu).onButtonClick(msg.button);
			}
		}, ctx.get());
	}

	public static interface IButtonContainer {
		void onButtonClick(int id);
	}

}

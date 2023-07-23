package shadows.placebo.packets;

import java.util.function.Supplier;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;

/**
 * Allows for easy implementations of client->server button presses. Sends an integer that allows for arbitrary data encoding schemes within the integer
 * space.<br>
 * The Container must implement {@link IButtonContainer}.<br>
 * Defer to using using {@link MultiPlayerGameMode#handleInventoryButtonClick} and {@link AbstractContainerMenu#clickMenuButton} when the buttonId can be a
 * byte.
 */
public class ButtonClickMessage implements MessageProvider<ButtonClickMessage> {

    int button;

    public ButtonClickMessage(int button) {
        this.button = button;
    }

    public ButtonClickMessage() {

    }

    @Override
    public Class<ButtonClickMessage> getMsgClass() {
        return ButtonClickMessage.class;
    }

    @Override
    public ButtonClickMessage read(FriendlyByteBuf buf) {
        return new ButtonClickMessage(buf.readInt());
    }

    @Override
    public void write(ButtonClickMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.button);
    }

    @Override
    public void handle(ButtonClickMessage msg, Supplier<Context> ctx) {
        MessageHelper.handlePacket(() -> () -> {
            if (ctx.get().getSender().containerMenu instanceof IButtonContainer) {
                ((IButtonContainer) ctx.get().getSender().containerMenu).onButtonClick(msg.button);
            }
        }, ctx);
    }

    public static interface IButtonContainer {
        void onButtonClick(int id);
    }

}

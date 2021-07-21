package shadows.placebo.util;

import java.util.Set;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CMarkRecipeSeenPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CQueryEntityNBTPacket;
import net.minecraft.network.play.client.CQueryTileEntityNBTPacket;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateBeaconPacket;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateJigsawBlockPacket;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.FakePlayer;

public class NetHandlerSpaghettiServer extends ServerPlayNetHandler {

	public NetHandlerSpaghettiServer(FakePlayer player) {
		super(null, new NetworkManager(PacketDirection.CLIENTBOUND), player);
	}

	@Override
	public void disconnect(ITextComponent textComponent) {
	}

	@Override
	public void onDisconnect(ITextComponent reason) {
	}

	@Override
	public void teleport(double x, double y, double z, float yaw, float pitch) {
	}

	@Override
	public void handleLockDifficulty(CLockDifficultyPacket p_217261_1_) {
	}

	@Override
	public void handleChangeDifficulty(CSetDifficultyPacket p_217263_1_) {
	}

	@Override
	public void handleSetJigsawBlock(CUpdateJigsawBlockPacket p_217262_1_) {
	}

	@Override
	public void handleAnimate(CAnimateHandPacket packetIn) {
	}

	@Override
	public void handleClientCommand(CClientStatusPacket packetIn) {
	}

	@Override
	public void handleMovePlayer(CPlayerPacket packetIn) {
	}

	@Override
	public void handleContainerButtonClick(CEnchantItemPacket packetIn) {
	}

	@Override
	public void handleContainerClose(CCloseWindowPacket packetIn) {
	}

	@Override
	public void handleSeenAdvancements(CSeenAdvancementsPacket packetIn) {
	}

	@Override
	public void handleMoveVehicle(CMoveVehiclePacket packetIn) {
	}

	@Override
	public void handleRecipeBookSeenRecipePacket(CMarkRecipeSeenPacket packetIn) {
	}

	@Override
	public void handleResourcePackResponse(CResourcePackStatusPacket packetIn) {
	}

	@Override
	public void handleChat(CChatMessagePacket packetIn) {
	}

	@Override
	public void handleTeleportToEntityPacket(CSpectatePacket packetIn) {
	}

	@Override
	public void handleClientInformation(CClientSettingsPacket packetIn) {
	}

	@Override
	public void handleContainerClick(CClickWindowPacket packetIn) {
	}

	@Override
	public void handleCustomPayload(CCustomPayloadPacket packetIn) {
	}

	@Override
	public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket packetIn) {
	}

	@Override
	public void handleAcceptTeleportPacket(CConfirmTeleportPacket packetIn) {
	}

	@Override
	public void handlePlayerCommand(CEntityActionPacket packetIn) {
	}

	@Override
	public void handleContainerAck(CConfirmTransactionPacket packetIn) {
	}

	@Override
	public void handlePlayerInput(CInputPacket packetIn) {
	}

	@Override
	public void handleEditBook(CEditBookPacket packetIn) {
	}

	@Override
	public void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket packetIn) {
	}

	@Override
	public void handleSetCarriedItem(CHeldItemChangePacket packetIn) {
	}

	@Override
	public void handlePlaceRecipe(CPlaceRecipePacket packetIn) {
	}

	@Override
	public void handleKeepAlive(CKeepAlivePacket packetIn) {
	}

	@Override
	public void handlePlayerAction(CPlayerDiggingPacket packetIn) {
	}

	@Override
	public void handleEntityTagQuery(CQueryEntityNBTPacket packetIn) {
	}

	@Override
	public void handleSelectTrade(CSelectTradePacket packetIn) {
	}

	@Override
	public void handlePickItem(CPickItemPacket packetIn) {
	}

	@Override
	public void handleCustomCommandSuggestions(CTabCompletePacket packetIn) {
	}

	@Override
	public void handlePlayerAbilities(CPlayerAbilitiesPacket packetIn) {
	}

	@Override
	public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket packetIn) {
	}

	@Override
	public void handlePaddleBoat(CSteerBoatPacket packetIn) {
	}

	@Override
	public void handleSetCommandBlock(CUpdateCommandBlockPacket packetIn) {
	}

	@Override
	public void handleRenameItem(CRenameItemPacket packetIn) {
	}

	@Override
	public void handleSignUpdate(CUpdateSignPacket packetIn) {
	}

	@Override
	public void handleUseItem(CPlayerTryUseItemPacket packetIn) {
	}

	@Override
	public void handleInteract(CUseEntityPacket packetIn) {
	}

	@Override
	public void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket packetIn) {
	}

	@Override
	public void handleSetStructureBlock(CUpdateStructureBlockPacket packetIn) {
	}

	@Override
	public void handleSetBeaconPacket(CUpdateBeaconPacket packetIn) {
	}

	@Override
	public void send(IPacket<?> packetIn, GenericFutureListener<? extends Future<? super Void>> futureListeners) {
	}

	@Override
	public void teleport(double x, double y, double z, float yaw, float pitch, Set<Flags> relativeSet) {
	}

	@Override
	public void send(IPacket<?> packetIn) {
	}

	@Override
	public void tick() {
	}

}

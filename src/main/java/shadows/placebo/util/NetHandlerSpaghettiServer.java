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
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
	}

	@Override
	public void func_217261_a(CLockDifficultyPacket p_217261_1_) {
	}

	@Override
	public void func_217263_a(CSetDifficultyPacket p_217263_1_) {
	}

	@Override
	public void func_217262_a(CUpdateJigsawBlockPacket p_217262_1_) {
	}

	@Override
	public void handleAnimation(CAnimateHandPacket packetIn) {
	}

	@Override
	public void processClientStatus(CClientStatusPacket packetIn) {
	}

	@Override
	public void processPlayer(CPlayerPacket packetIn) {
	}

	@Override
	public void processEnchantItem(CEnchantItemPacket packetIn) {
	}

	@Override
	public void processCloseWindow(CCloseWindowPacket packetIn) {
	}

	@Override
	public void handleSeenAdvancements(CSeenAdvancementsPacket packetIn) {
	}

	@Override
	public void processVehicleMove(CMoveVehiclePacket packetIn) {
	}

	@Override
	public void handleRecipeBookUpdate(CMarkRecipeSeenPacket packetIn) {
	}

	@Override
	public void handleResourcePackStatus(CResourcePackStatusPacket packetIn) {
	}

	@Override
	public void processChatMessage(CChatMessagePacket packetIn) {
	}

	@Override
	public void handleSpectate(CSpectatePacket packetIn) {
	}

	@Override
	public void processClientSettings(CClientSettingsPacket packetIn) {
	}

	@Override
	public void processClickWindow(CClickWindowPacket packetIn) {
	}

	@Override
	public void processCustomPayload(CCustomPayloadPacket packetIn) {
	}

	@Override
	public void processCreativeInventoryAction(CCreativeInventoryActionPacket packetIn) {
	}

	@Override
	public void processConfirmTeleport(CConfirmTeleportPacket packetIn) {
	}

	@Override
	public void processEntityAction(CEntityActionPacket packetIn) {
	}

	@Override
	public void processConfirmTransaction(CConfirmTransactionPacket packetIn) {
	}

	@Override
	public void processInput(CInputPacket packetIn) {
	}

	@Override
	public void processEditBook(CEditBookPacket packetIn) {
	}

	@Override
	public void processNBTQueryBlockEntity(CQueryTileEntityNBTPacket packetIn) {
	}

	@Override
	public void processHeldItemChange(CHeldItemChangePacket packetIn) {
	}

	@Override
	public void processPlaceRecipe(CPlaceRecipePacket packetIn) {
	}

	@Override
	public void processKeepAlive(CKeepAlivePacket packetIn) {
	}

	@Override
	public void processPlayerDigging(CPlayerDiggingPacket packetIn) {
	}

	@Override
	public void processNBTQueryEntity(CQueryEntityNBTPacket packetIn) {
	}

	@Override
	public void processSelectTrade(CSelectTradePacket packetIn) {
	}

	@Override
	public void processPickItem(CPickItemPacket packetIn) {
	}

	@Override
	public void processTabComplete(CTabCompletePacket packetIn) {
	}

	@Override
	public void processPlayerAbilities(CPlayerAbilitiesPacket packetIn) {
	}

	@Override
	public void processTryUseItemOnBlock(CPlayerTryUseItemOnBlockPacket packetIn) {
	}

	@Override
	public void processSteerBoat(CSteerBoatPacket packetIn) {
	}

	@Override
	public void processUpdateCommandBlock(CUpdateCommandBlockPacket packetIn) {
	}

	@Override
	public void processRenameItem(CRenameItemPacket packetIn) {
	}

	@Override
	public void processUpdateSign(CUpdateSignPacket packetIn) {
	}

	@Override
	public void processTryUseItem(CPlayerTryUseItemPacket packetIn) {
	}

	@Override
	public void processUseEntity(CUseEntityPacket packetIn) {
	}

	@Override
	public void processUpdateCommandMinecart(CUpdateMinecartCommandBlockPacket packetIn) {
	}

	@Override
	public void processUpdateStructureBlock(CUpdateStructureBlockPacket packetIn) {
	}

	@Override
	public void processUpdateBeacon(CUpdateBeaconPacket packetIn) {
	}

	@Override
	public void sendPacket(IPacket<?> packetIn, GenericFutureListener<? extends Future<? super Void>> futureListeners) {
	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<Flags> relativeSet) {
	}

	@Override
	public void sendPacket(IPacket<?> packetIn) {
	}

	@Override
	public void tick() {
	}

}

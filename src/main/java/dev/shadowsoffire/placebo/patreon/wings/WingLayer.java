package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

public class WingLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final ContextKey<WingRenderData> WING_DATA = new ContextKey<>(Placebo.loc("wings/render_data"));

    public static void registerModifier(RegisterRenderStateModifiersEvent e) {
        e.registerAvatarEntityModifier(new AvatarRenderStateModifier(){
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                if (avatar instanceof AbstractClientPlayer player) {

                    if (WingsManager.DISABLED.contains(player.getUUID())) {
                        return;
                    }
                    WingType type = WingsManager.getType(player.getUUID());
                    if (type != null) {
                        state.setRenderData(WING_DATA, new WingRenderData(type, type.textureGetter.apply(player)));
                    }
                }
            }
        });
    }

    public WingLayer(RenderLayerParent<AvatarRenderState, PlayerModel> playerRenderer) {
        super(playerRenderer);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        WingRenderData data = state.getRenderData(WING_DATA);
        if (data == null) {
            return;
        }

        stack.pushPose();
        stack.translate(0, data.type().yOffset, 0);
        data.type().model.get().submit(stack, collector, lightCoords, state, data.texture());
        stack.popPose();
    }

    public record WingRenderData(WingType type, Identifier texture) {}

}

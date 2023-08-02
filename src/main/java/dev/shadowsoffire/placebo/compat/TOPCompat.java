package dev.shadowsoffire.placebo.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dev.shadowsoffire.placebo.Placebo;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;

/**
 * Stupid boilerplate for The One Probe.
 */
public class TOPCompat {

    public static void register() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
    }

    private static List<Provider> providers = new ArrayList<>();

    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        @Override
        public Void apply(ITheOneProbe probe) {
            probe.registerProvider(new IProbeInfoProvider(){
                @Override
                public ResourceLocation getID() {
                    return new ResourceLocation(Placebo.MODID, "plugin");
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {
                    providers.forEach(p -> p.addProbeInfo(mode, info, player, level, state, hitData));
                }
            });
            probe.registerEntityProvider(new IProbeInfoEntityProvider(){
                @Override
                public String getID() {
                    return Placebo.MODID + ":plugin";
                }

                @Override
                public void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, Entity entity, IProbeHitEntityData hitData) {
                    providers.forEach(p -> p.addProbeEntityInfo(mode, info, player, level, entity, hitData));
                }
            });
            return null;
        }

    }

    public static void registerProvider(Provider p) {
        providers.add(p);
    }

    public static interface Provider {
        default void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hitData) {}

        default void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, Entity entity, IProbeHitEntityData hitData) {}
    }

}

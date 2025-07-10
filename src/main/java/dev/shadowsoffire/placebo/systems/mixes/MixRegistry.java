package dev.shadowsoffire.placebo.systems.mixes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.PlaceboClient;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.systems.mixes.JsonMix.Type;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class MixRegistry extends DynamicRegistry<JsonMix<?>> {

    public static final MixRegistry INSTANCE = new MixRegistry();

    public MixRegistry() {
        super(Placebo.LOGGER, "brewing_mixes", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("mix"), JsonMix.CODEC);
    }

    @Override
    protected void beginReload(ReloadType type) {
        for (PotionBrewing brewing : resolveBrewing()) {
            removeAll(brewing);
        }
        super.beginReload(type);
    }

    @Override
    protected void onReload(ReloadType type) {
        for (PotionBrewing brewing : resolveBrewing()) {
            addAll(brewing);
        }
        super.onReload(type);
    }

    /**
     * Called externally during the {@link ServerAboutToStartEvent} since the first reload on dedi is too early.
     */
    public static void applyMixes() {
        for (PotionBrewing brewing : resolveBrewing()) {
            INSTANCE.addAll(brewing);
        }
    }

    /**
     * Attempts to resolve the {@link PotionBrewing} instances from the given global context.
     * <p>
     * These are nullable because it fails to resolve during world creation in singleplayer, since an instance has not been created yet.
     */
    private static List<@Nullable PotionBrewing> resolveBrewing() {
        List<PotionBrewing> registries = new ArrayList<>();
        if (FMLEnvironment.dist.isClient()) {
            registries.add(PlaceboClient.getBrewingRegistry());
        }

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            registries.add(ServerLifecycleHooks.getCurrentServer().potionBrewing());
        }

        return registries;
    }

    @SuppressWarnings("unchecked")
    private static List<PotionBrewing.Mix<?>> getMixList(PotionBrewing brewing, Type type) {
        return (List<PotionBrewing.Mix<?>>) (Object) switch (type) {
            case POTION -> brewing.potionMixes;
            case CONTAINER -> brewing.containerMixes;
        };
    }

    private static void makeMutable(PotionBrewing brewing) {
        brewing.containerMixes = new ArrayList<>(brewing.containerMixes);
        brewing.potionMixes = new ArrayList<>(brewing.potionMixes);
    }

    private void removeAll(@Nullable PotionBrewing brewing) {
        if (brewing != null) {
            makeMutable(brewing);
            this.getValues().forEach(mix -> {
                getMixList(brewing, mix.type()).remove(mix.mix());
            });
        }
    }

    private void addAll(@Nullable PotionBrewing brewing) {
        if (brewing != null) {
            makeMutable(brewing);
            this.getValues().forEach(mix -> {
                getMixList(brewing, mix.type()).add(mix.mix());
            });
        }
    }

}

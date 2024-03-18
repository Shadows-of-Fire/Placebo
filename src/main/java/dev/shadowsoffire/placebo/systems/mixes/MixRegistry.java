package dev.shadowsoffire.placebo.systems.mixes;

import java.util.List;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.systems.mixes.JsonMix.Type;
import net.minecraft.world.item.alchemy.PotionBrewing;

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
    protected void beginReload() {
        this.getValues().forEach(mix -> {
            getMixList(mix.getMixType()).remove(mix);
        });
        super.beginReload();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onReload() {
        super.onReload();
        this.getValues().forEach(mix -> {
            getMixList(mix.getMixType()).add(mix);
        });
    }

    @SuppressWarnings("rawtypes")
    private static List getMixList(Type type) {
        return switch (type) {
            case POTION -> PotionBrewing.POTION_MIXES;
            case CONTAINER -> PotionBrewing.CONTAINER_MIXES;
        };
    }

}

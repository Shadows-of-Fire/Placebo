package dev.shadowsoffire.placebo.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.json.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

public class GearSetRegistry extends WeightedDynamicRegistry<GearSet> {

    public static final GearSetRegistry INSTANCE = new GearSetRegistry();

    public GearSetRegistry() {
        super(Placebo.LOGGER, "gear_sets", false, false);
    }

    /**
     * Returns a random weighted armor set based on the given random (and predicate, if applicable).
     */
    public <T extends Predicate<GearSet>> GearSet getRandomSet(RandomSource rand, float luck, @Nullable List<SetPredicate> armorSets) {
        if (armorSets == null || armorSets.isEmpty()) return this.getRandomItem(rand, luck);
        List<GearSet> valid = this.registry.values().stream().filter(e -> {
            for (Predicate<GearSet> f : armorSets)
                if (f.test(e)) return true;
            return false;
        }).collect(Collectors.toList());
        if (valid.isEmpty()) {
            Placebo.LOGGER.error("Failed to locate any gear sets matching the following predicates: ");
            armorSets.forEach(s -> Placebo.LOGGER.error(s.toString()));
            return this.getRandomItem(rand, luck);
        }

        List<Wrapper<GearSet>> list = new ArrayList<>(valid.size());
        valid.stream().map(l -> l.<GearSet>wrap(luck)).forEach(list::add);
        return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(new ResourceLocation(Placebo.MODID, "default"), GearSet.CODEC);
    }

}

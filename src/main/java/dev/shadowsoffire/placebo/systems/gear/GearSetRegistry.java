package dev.shadowsoffire.placebo.systems.gear;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;

public class GearSetRegistry extends WeightedDynamicRegistry<GearSet> {

    public static final GearSetRegistry INSTANCE = new GearSetRegistry();

    public GearSetRegistry() {
        super(Placebo.LOGGER, "gear_sets", false, false);
    }

    /**
     * Returns a random weighted armor set based on the given random (and predicate, if applicable).
     */
    @Nullable
    public GearSet getRandomSet(RandomSource rand, float luck, @Nullable List<SetPredicate> armorSets) {
        if (armorSets == null || armorSets.isEmpty()) {
            return this.getRandomItem(rand, luck);
        }
        List<GearSet> valid = this.registry.values().stream().filter(e -> {
            for (Predicate<GearSet> f : armorSets) {
                if (f.test(e)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        if (valid.isEmpty()) {
            Placebo.LOGGER.error("Failed to locate any gear sets matching the following predicates: ");
            armorSets.forEach(s -> Placebo.LOGGER.error(s.toString()));
            return this.getRandomItem(rand, luck);
        }
        WeightedList.Builder<GearSet> builder = WeightedList.builder();
        for (GearSet item : valid) {
            int weight = Math.max(0, item.getWeight() + (int) (luck * item.getQuality()));
            if (weight > 0) {
                builder.add(item, weight);
            }
        }
        return builder.build().getRandom(rand).orElse(null);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("gear_set"), GearSet.CODEC);
    }

}

package dev.shadowsoffire.placebo.systems.wanderer;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

/**
 * Interface for wandering trader trades. Same as {@link ItemListing} with a flag for rarity.
 */
public interface WandererTrade extends ItemListing, CodecProvider<WandererTrade> {

    boolean isRare();

}

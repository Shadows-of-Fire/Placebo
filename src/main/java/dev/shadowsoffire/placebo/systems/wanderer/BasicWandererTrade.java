package dev.shadowsoffire.placebo.systems.wanderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;

public class BasicWandererTrade extends BasicItemListing implements WandererTrade {

    public static Codec<BasicWandererTrade> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ItemAdapter.CODEC.fieldOf("input_1").forGetter(trade -> trade.price),
            ExtraCodecs.strictOptionalField(ItemAdapter.CODEC, "input_2", ItemStack.EMPTY).forGetter(trade -> trade.price2),
            ItemAdapter.CODEC.fieldOf("output").forGetter(trade -> trade.forSale),
            ExtraCodecs.strictOptionalField(Codec.INT, "max_trades", 1).forGetter(trade -> trade.maxTrades),
            ExtraCodecs.strictOptionalField(Codec.INT, "xp", 0).forGetter(trade -> trade.xp),
            ExtraCodecs.strictOptionalField(Codec.FLOAT, "price_mult", 1F).forGetter(trade -> trade.priceMult),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "rare", false).forGetter(trade -> trade.rare))
        .apply(inst, BasicWandererTrade::new));

    protected final boolean rare;

    public BasicWandererTrade(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult, boolean rare) {
        super(price, price2, forSale, maxTrades, xp, priceMult);
        this.rare = rare;
    }

    @Override
    public boolean isRare() {
        return this.rare;
    }

    @Override
    public Codec<? extends WandererTrade> getCodec() {
        return CODEC;
    }

}

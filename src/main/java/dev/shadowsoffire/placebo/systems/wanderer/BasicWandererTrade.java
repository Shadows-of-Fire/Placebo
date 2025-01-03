package dev.shadowsoffire.placebo.systems.wanderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.OptionalStackCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;

public class BasicWandererTrade extends BasicItemListing implements WandererTrade {

    public static Codec<BasicWandererTrade> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            OptionalStackCodec.INSTANCE.fieldOf("input_1").forGetter(trade -> trade.price),
            OptionalStackCodec.INSTANCE.optionalFieldOf("input_2", ItemStack.EMPTY).forGetter(trade -> trade.price2),
            OptionalStackCodec.INSTANCE.fieldOf("output").forGetter(trade -> trade.forSale),
            Codec.INT.optionalFieldOf("max_trades", 1).forGetter(trade -> trade.maxTrades),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(trade -> trade.xp),
            Codec.FLOAT.optionalFieldOf("price_mult", 1F).forGetter(trade -> trade.priceMult),
            Codec.BOOL.optionalFieldOf("rare", false).forGetter(trade -> trade.rare))
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ItemStack price = ItemStack.EMPTY;
        private ItemStack price2 = ItemStack.EMPTY;
        private ItemStack forSale = ItemStack.EMPTY;
        private int maxTrades = 1;
        private int xp = 0;
        private float priceMult = 1F;
        private boolean rare = false;

        public Builder price(ItemStack price) {
            this.price = price;
            return this;
        }

        public Builder price(Item price, int count) {
            return price(new ItemStack(price, count));
        }

        public Builder price2(ItemStack price2) {
            this.price2 = price2;
            return this;
        }

        public Builder price2(Item price, int count) {
            return price2(new ItemStack(price, count));
        }

        public Builder forSale(ItemStack forSale) {
            this.forSale = forSale;
            return this;
        }

        public Builder forSale(Item output, int count) {
            return forSale(new ItemStack(output, count));
        }

        public Builder maxTrades(int maxTrades) {
            this.maxTrades = maxTrades;
            return this;
        }

        public Builder xp(int xp) {
            this.xp = xp;
            return this;
        }

        public Builder priceMult(float priceMult) {
            this.priceMult = priceMult;
            return this;
        }

        public Builder rare() {
            this.rare = true;
            return this;
        }

        public BasicWandererTrade build() {
            return new BasicWandererTrade(price, price2, forSale, maxTrades, xp, priceMult, rare);
        }
    }

}

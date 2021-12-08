package shadows.placebo.recipe;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;

public class TagIngredient extends Ingredient {

	public static final IIngredientSerializer<Ingredient> SERIALIZER = new VanillaIngredientSerializer();

	protected String tagId;
	protected Tag<Item> tag;
	protected ItemStack[] stacks;
	protected IntList matchingStacksPacked;

	public TagIngredient(String tag) {
		super(Stream.empty());
		this.tagId = tag;
		this.stacks = new ItemStack[0];
	}

	@Override
	public boolean test(ItemStack stack) {
		return this.tag().contains(stack.getItem());
	}

	@Override
	public ItemStack[] getItems() {
		if (this.tag().getValues().size() != this.stacks.length) {
			this.stacks = this.tag().getValues().stream().map(ItemStack::new).collect(Collectors.toList()).toArray(new ItemStack[0]);
		}
		return this.stacks;
	}

	@Override
	public IntList getStackingIds() {
		if (this.matchingStacksPacked == null) {
			ItemStack[] matchingStacks = this.getItems();
			this.matchingStacksPacked = new IntArrayList(matchingStacks.length);
			for (ItemStack itemstack : matchingStacks) {
				this.matchingStacksPacked.add(StackedContents.getStackingIndex(itemstack));
			}
			this.matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
		}
		return this.matchingStacksPacked;
	}

	@Override
	public boolean isEmpty() {
		return this.tag().getValues().isEmpty();
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	protected Tag<Item> tag() {
		return this.tag != null ? this.tag : (this.tag = ItemTags.bind(this.tagId));
	}

	protected void redefine() {
		this.tag = SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY).getTag(new ResourceLocation(this.tagId));
	}

}

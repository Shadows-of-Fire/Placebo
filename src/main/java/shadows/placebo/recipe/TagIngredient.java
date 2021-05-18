package shadows.placebo.recipe;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;

public class TagIngredient extends Ingredient {

	public static final IIngredientSerializer<Ingredient> SERIALIZER = new VanillaIngredientSerializer();

	protected String tagId;
	protected ITag<Item> tag;
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
	public ItemStack[] getMatchingStacks() {
		if (this.tag().getAllElements().size() != this.stacks.length) {
			this.stacks = this.tag().getAllElements().stream().map(ItemStack::new).collect(Collectors.toList()).toArray(new ItemStack[0]);
		}
		return this.stacks;
	}

	@Override
	public IntList getValidItemStacksPacked() {
		if (this.matchingStacksPacked == null) {
			ItemStack[] matchingStacks = this.getMatchingStacks();
			this.matchingStacksPacked = new IntArrayList(matchingStacks.length);
			for (ItemStack itemstack : matchingStacks) {
				this.matchingStacksPacked.add(RecipeItemHelper.pack(itemstack));
			}
			this.matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
		}
		return this.matchingStacksPacked;
	}

	@Override
	public boolean hasNoMatchingItems() {
		return this.tag().getAllElements().isEmpty();
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	protected ITag<Item> tag() {
		return this.tag != null ? this.tag : (this.tag = ItemTags.makeWrapperTag(this.tagId));
	}

	protected void redefine() {
		this.tag = TagCollectionManager.getManager().getItemTags().get(new ResourceLocation(this.tagId));
	}

}

package shadows.placebo.recipe;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class TagIngredient extends Ingredient {

	public static final IIngredientSerializer<TagIngredient> SERIALIZER = new IIngredientSerializer<TagIngredient>() {

		@Override
		public TagIngredient parse(PacketBuffer buffer) {
			ResourceLocation tag = new ResourceLocation(buffer.readString(32767));
			return new TagIngredient(tag);
		}

		@Override
		public TagIngredient parse(JsonObject json) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(PacketBuffer buffer, TagIngredient ingredient) {
			if(ingredient.tag instanceof ITag.INamedTag) {
				ITag.INamedTag<Item> tag = (ITag.INamedTag<Item>) ingredient.tag;
				// TODO MCP-name: func_230234_a_ -> getId
				buffer.writeString(tag.func_230234_a_().toString());
			}
		}

	};

	protected ResourceLocation tagId;
	protected ITag<Item> tag;
	protected ItemStack[] stacks;

	public TagIngredient(ITag<Item> tag) {
		super(Stream.of(new Ingredient.TagList(tag)));
		this.tag = tag;
		this.stacks = new ItemStack[0];
	}

	public TagIngredient(ResourceLocation tag) {
		this(ItemTags.makeWrapperTag(tag.toString()));
	}

	@Override
	public boolean test(ItemStack stack) {
		// TODO MCP-name: func_230235_a_ -> contains
		return tag.func_230235_a_(stack.getItem());
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		// TODO MCP-name: func_230236_b_ -> getAllElements
		if (tag.func_230236_b_().size() != stacks.length) {
			// TODO MCP-name: func_230236_b_ -> getAllElements
			stacks = tag.func_230236_b_().stream().map(ItemStack::new).collect(Collectors.toList()).toArray(new ItemStack[0]);
		}
		return stacks;
	}

	@Override
	// TODO MCP-name: func_230236_b_ -> getAllElements
	public boolean hasNoMatchingItems() {
		return tag.func_230236_b_().isEmpty();
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

}

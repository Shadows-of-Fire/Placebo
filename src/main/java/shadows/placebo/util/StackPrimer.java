package shadows.placebo.util;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * The StackPrimer is an ItemStack generator, basically holding all the information to create an ItemStack instance without ever retaining one.
 */
public class StackPrimer {

	Item item = null;
	Block block;
	int size;
	CompoundTag tag;

	public StackPrimer(Item item, int size, @Nullable CompoundTag tag) {
		this.item = item;
		this.size = size;
		this.tag = tag;
	}

	public StackPrimer(Item item, @Nullable CompoundTag tag) {
		this(item, 1, tag);
	}

	public StackPrimer(Item item) {
		this(item, 1, null);
	}

	public StackPrimer(Block block, int size, @Nullable CompoundTag tag) {
		this.block = block;
		this.size = size;
		this.tag = tag;
	}

	public StackPrimer(Block item, @Nullable CompoundTag tag) {
		this(item, 1, tag);
	}

	public StackPrimer(Block item) {
		this(item, 1, null);
	}

	public ItemStack genStack() {
		ItemStack stack = new ItemStack(this.item == null ? this.item = this.block.asItem() : this.item, this.size);
		stack.setTag(this.tag.copy());
		return stack;
	}

	public boolean isEmpty() {
		return this.item == null || this.size <= 0;
	}

	@Nullable
	public Block getBlock() {
		return this.block;
	}

	@Nullable
	public Item getItem() {
		return this.item;
	}

	public int getCount() {
		return this.size;
	}

}

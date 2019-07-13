package shadows.placebo.util;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class StackPrimer {

	Item item = null;
	Block block;
	int size;
	CompoundNBT tag;

	public StackPrimer(Item item, int size, @Nullable CompoundNBT tag) {
		this.item = item;
		this.size = size;
		this.tag = tag;
	}

	public StackPrimer(Item item, @Nullable CompoundNBT tag) {
		this(item, 1, tag);
	}

	public StackPrimer(Item item) {
		this(item, 1, null);
	}

	public StackPrimer(Block block, int size, @Nullable CompoundNBT tag) {
		this.block = block;
		this.size = size;
		this.tag = tag;
	}

	public StackPrimer(Block item, @Nullable CompoundNBT tag) {
		this(item, 1, tag);
	}

	public StackPrimer(Block item) {
		this(item, 1, null);
	}

	public ItemStack genStack() {
		ItemStack stack = new ItemStack(item == null ? item = block.asItem() : item, size);
		stack.setTag(tag.copy());
		return stack;
	}

	public boolean isEmpty() {
		return item == null || size <= 0;
	}

	@Nullable
	public Block getBlock() {
		return block;
	}

	@Nullable
	public Item getItem() {
		return item;
	}

	public int getCount() {
		return size;
	}

}

package shadows.placebo.util;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/**
 * Utils for loot chests. Helps with placing and 
 * @author Shadows
 *
 */
public class ChestBuilder {

	protected Random random;
	protected ChestBlockEntity chest;
	protected boolean isValid;
	protected BlockPos position;
	protected LevelAccessor iWorld;

	private ChestBuilder(LevelAccessor world, Random rand, BlockPos pos) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof ChestBlockEntity) {
			this.random = rand;
			this.chest = (ChestBlockEntity) tileEntity;
			this.isValid = true;
			this.position = pos;
			this.iWorld = world;
		}
	}

	public void fill(ResourceLocation loot) {
		RandomizableContainerBlockEntity.setLootTable(this.iWorld, this.random, this.position, loot);
	}

	public static void place(LevelAccessor world, Random random, BlockPos pos, ResourceLocation loot) {
		world.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
		ChestBuilder chest = new ChestBuilder(world, random, pos);
		if (chest.isValid) {
			chest.fill(loot);
		}
	}

	public static void placeTrapped(LevelAccessor world, Random random, BlockPos pos, ResourceLocation loot) {
		world.setBlock(pos, Blocks.TRAPPED_CHEST.defaultBlockState(), 2);
		ChestBuilder chest = new ChestBuilder(world, random, pos);
		if (chest.isValid) {
			chest.fill(loot);
		}
	}

}
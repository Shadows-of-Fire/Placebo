package shadows.placebo.util;

import javax.annotation.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Util class to edit spawners in world.  Constructing one will create a spawner if not present.
 * @author Shadows
 *
 */
public class SpawnerEditor {

	//Default variables for configurable spawner stats.
	public static final int SPAWN_DELAY = 20;
	public static final int MIN_SPAWN_DELAY = 200;
	public static final int MAX_SPAWN_DELAY = 800;
	public static final int SPAWN_COUNT = 4;
	public static final int MAX_NEARBY_ENTITIES = 6;
	public static final int PLAYER_RANGE = 16;
	public static final int SPAWN_RANGE = 4;

	protected MobSpawnerTileEntity spawner;

	public SpawnerEditor(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof MobSpawnerTileEntity) {
			spawner = (MobSpawnerTileEntity) te;
		} else {
			world.setBlockState(pos, Blocks.SPAWNER.getDefaultState());
			te = world.getTileEntity(pos);
			spawner = (MobSpawnerTileEntity) te;
		}
	}

	/**
	 * Sets the mob type of the first spawn (or all spawns if potentials are not set).
	 */
	public SpawnerEditor setType(EntityType<? extends Entity> entity) {
		this.spawner.getSpawnerBaseLogic().setEntityType(entity);
		return this;
	}

	/**
	 * Sets the delay before the first spawn. Set to -1 to skip first spawn.
	 */
	public SpawnerEditor setDelay(int delay) {
		this.spawner.getSpawnerBaseLogic().spawnDelay = delay;
		return this;
	}

	/**
	 * Sets min spawn delay.
	 */
	public SpawnerEditor setMinDelay(int min) {
		this.spawner.getSpawnerBaseLogic().minSpawnDelay = min;
		return this;
	}

	/**
	 * Sets max spawn delay.
	 */
	public SpawnerEditor setMaxDelay(int max) {
		this.spawner.getSpawnerBaseLogic().maxSpawnDelay = max;
		return this;
	}

	/**
	 * Sets min and max spawn delays.
	 */
	public SpawnerEditor setMinAndMaxDelay(int min, int max) {
		this.setMinDelay(min);
		this.setMaxDelay(max);
		return this;
	}

	/**
	 * Sets the number of spawn attempts.
	 */
	public SpawnerEditor setSpawnCount(int count) {
		this.spawner.getSpawnerBaseLogic().spawnCount = count;
		return this;
	}

	/**
	 * Sets the max nearby entities.
	 */
	public SpawnerEditor setMaxNearbyEntities(int max) {
		this.spawner.getSpawnerBaseLogic().maxNearbyEntities = max;
		return this;
	}

	/**
	 * Sets the required player radius (in blocks) to activate.
	 */
	public SpawnerEditor setPlayerRange(int range) {
		this.spawner.getSpawnerBaseLogic().activatingRangeFromPlayer = range;
		return this;
	}

	/**
	 * Sets the spawn radius (in blocks).
	 */
	public SpawnerEditor setSpawnRange(int range) {
		this.spawner.getSpawnerBaseLogic().spawnRange = range;
		return this;
	}

	/**
	 * Sets the additional NBT data for the first mob spawned (or all, if potentials are not set).
	 * @param data An entity, written to NBT, in the format read by AnvilChunkLoader.readWorldEntity()
	 */
	public SpawnerEditor setSpawnData(int weight, @Nullable CompoundNBT data) {
		if (data == null) this.spawner.getSpawnerBaseLogic().spawnData = new WeightedSpawnerEntity();
		else this.spawner.getSpawnerBaseLogic().spawnData = new WeightedSpawnerEntity(weight, data);
		return this;
	}

	/*
	 * Sets the list of entities the mob spawner will choose from.
	 */
	public SpawnerEditor setPotentials(WeightedSpawnerEntity... entries) {
		this.spawner.getSpawnerBaseLogic().potentialSpawns.clear();
		for (WeightedSpawnerEntity e : entries)
			this.spawner.getSpawnerBaseLogic().potentialSpawns.add(e);
		return this;
	}
}
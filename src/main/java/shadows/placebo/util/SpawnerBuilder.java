package shadows.placebo.util;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Util class to create TileEntityMobSpawner nbt tags.
 * @author Shadows
 *
 */
public class SpawnerBuilder {

	public static final String SPAWN_DELAY = "Delay";
	public static final String SPAWN_POTENTIALS = "SpawnPotentials";
	public static final String SPAWN_DATA = "SpawnData";
	public static final String MIN_SPAWN_DELAY = "MinSpawnDelay";
	public static final String MAX_SPAWN_DELAY = "MaxSpawnDelay";
	public static final String SPAWN_COUNT = "SpawnCount";
	public static final String MAX_NEARBY_ENTITIES = "MaxNearbyEntities";
	public static final String REQUIRED_PLAYER_RANGE = "RequiredPlayerRange";
	public static final String SPAWN_RANGE = "SpawnRange";
	public static final String ID = "id";
	public static final String ENTITY = "Entity"; //WeightedSpawnerEntity's internal entity tag
	public static final CompoundNBT BASE_TAG;
	static {
		MobSpawnerTileEntity te = (MobSpawnerTileEntity) Blocks.SPAWNER.createTileEntity(null, null);
		te.getSpawnerBaseLogic().setEntityType(EntityType.PIG);
		BASE_TAG = te.write(new CompoundNBT());
	}

	CompoundNBT tag = BASE_TAG.copy();
	boolean hasPotentials = false;
	WeightedSpawnerEntity baseEntity = new WeightedSpawnerEntity();

	public SpawnerBuilder() {
		tag.put(SPAWN_DATA, baseEntity.getNbt());
	}

	/**
	 * Sets the mob type of the first spawn (or all spawns if potentials are not set).
	 */
	public SpawnerBuilder setType(EntityType<? extends Entity> entity) {
		return setType(entity.getRegistryName());
	}

	/**
	 * Sets the mob type of the first spawn (or all spawns if potentials are not set).
	 */
	public SpawnerBuilder setType(ResourceLocation entity) {
		baseEntity.getNbt().putString(ID, entity.toString());
		return this;
	}

	/**
	 * Sets the delay before the first spawn. Set to -1 to skip first spawn.
	 */
	public SpawnerBuilder setDelay(int delay) {
		tag.putShort(SPAWN_DELAY, (short) delay);
		return this;
	}

	/**
	 * Sets min spawn delay.
	 */
	public SpawnerBuilder setMinDelay(int delay) {
		tag.putShort(MIN_SPAWN_DELAY, (short) delay);
		return this;
	}

	/**
	 * Sets max spawn delay.
	 */
	public SpawnerBuilder setMaxDelay(int delay) {
		tag.putShort(MAX_SPAWN_DELAY, (short) delay);
		return this;
	}

	/**
	 * Sets min and max spawn delays.
	 */
	public SpawnerBuilder setMinAndMaxDelay(int min, int max) {
		this.setMinDelay(min);
		this.setMaxDelay(max);
		return this;
	}

	/**
	 * Sets the number of spawn attempts.
	 */
	public SpawnerBuilder setSpawnCount(int count) {
		tag.putShort(SPAWN_COUNT, (short) count);
		return this;
	}

	/**
	 * Sets the max nearby entities.
	 */
	public SpawnerBuilder setMaxNearbyEntities(int max) {
		tag.putShort(MAX_NEARBY_ENTITIES, (short) max);
		return this;
	}

	/**
	 * Sets the required player radius (in blocks) to activate.
	 */
	public SpawnerBuilder setPlayerRange(int range) {
		tag.putShort(REQUIRED_PLAYER_RANGE, (short) range);
		return this;
	}

	/**
	 * Sets the spawn radius (in blocks).
	 */
	public SpawnerBuilder setSpawnRange(int range) {
		tag.putShort(SPAWN_RANGE, (short) range);
		return this;
	}

	/**
	 * Sets the additional NBT data for the first mob spawned (or all, if potentials are not set).
	 * @param data An entity, written to NBT, in the format read by AnvilChunkLoader.readWorldEntity()
	 */
	public SpawnerBuilder setSpawnData(CompoundNBT data) {
		if (data == null) {
			data = new CompoundNBT();
			data.putString(ID, "minecraft:pig");
		}
		baseEntity.nbt = data.copy();
		return this;
	}

	/*
	 * Sets the list of entities the mob spawner will choose from.
	 */
	public SpawnerBuilder setPotentials(WeightedSpawnerEntity... entries) {
		hasPotentials = true;
		tag.put(SPAWN_POTENTIALS, new ListNBT());
		ListNBT list = tag.getList(SPAWN_POTENTIALS, 10);
		for (WeightedSpawnerEntity e : entries)
			list.add(e.toCompoundTag());
		return this;
	}

	/*
	 * Adds to the list of entities the mob spawner will choose from.
	 */
	public SpawnerBuilder addPotentials(WeightedSpawnerEntity... entries) {
		hasPotentials = true;
		ListNBT list = tag.getList(SPAWN_POTENTIALS, 10);
		for (WeightedSpawnerEntity e : entries)
			list.add(e.toCompoundTag());
		return this;
	}

	/**
	 * @return The spawn data, represented as an entity nbt tag.
	 */
	public CompoundNBT getSpawnData() {
		return tag.getCompound(SPAWN_DATA);
	}

	/**
	 * @return The spawn data, represented as an entity nbt tag.
	 */
	public ListNBT getPoten1tials() {
		return tag.getList(SPAWN_POTENTIALS, 10);
	}

	public MobSpawnerTileEntity build(World world, BlockPos pos) {
		MobSpawnerTileEntity s = (MobSpawnerTileEntity) Blocks.SPAWNER.createTileEntity(null, world);
		if (!hasPotentials) {
			ListNBT list = new ListNBT();
			list.add(baseEntity.toCompoundTag());
			tag.put(SPAWN_POTENTIALS, list);
		}
		s.setWorld(world);
		s.setPos(pos);
		s.read(tag);
		return s;
	}
}
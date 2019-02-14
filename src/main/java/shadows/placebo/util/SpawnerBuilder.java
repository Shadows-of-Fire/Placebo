package shadows.placebo.util;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityMobSpawner;
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
	public static final NBTTagCompound BASE_TAG;
	static {
		TileEntityMobSpawner te = (TileEntityMobSpawner) ((ITileEntityProvider) Blocks.MOB_SPAWNER).createNewTileEntity(null, 0);
		te.getSpawnerBaseLogic().setEntityId(new ResourceLocation("minecraft:pig"));
		BASE_TAG = te.writeToNBT(new NBTTagCompound());
	}

	NBTTagCompound tag = BASE_TAG.copy();
	boolean hasPotentials = false;
	WeightedSpawnerEntity baseEntity = new WeightedSpawnerEntity();

	public SpawnerBuilder() {
		tag.setTag(SPAWN_DATA, baseEntity.getNbt());
	}

	/**
	 * Sets the mob type of the first spawn (or all spawns if potentials are not set).
	 */
	public SpawnerBuilder setType(Class<? extends Entity> entity) {
		return setType(EntityList.getKey(entity));
	}

	/**
	 * Sets the mob type of the first spawn (or all spawns if potentials are not set).
	 */
	public SpawnerBuilder setType(ResourceLocation entity) {
		baseEntity.getNbt().setString(ID, entity.toString());
		return this;
	}

	/**
	 * Sets the delay before the first spawn. Set to -1 to skip first spawn.
	 */
	public SpawnerBuilder setDelay(int delay) {
		tag.setShort(SPAWN_DELAY, (short) delay);
		return this;
	}

	/**
	 * Sets min spawn delay.
	 */
	public SpawnerBuilder setMinDelay(int delay) {
		tag.setShort(MIN_SPAWN_DELAY, (short) delay);
		return this;
	}

	/**
	 * Sets max spawn delay.
	 */
	public SpawnerBuilder setMaxDelay(int delay) {
		tag.setShort(MAX_SPAWN_DELAY, (short) delay);
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
		tag.setShort(SPAWN_COUNT, (short) count);
		return this;
	}

	/**
	 * Sets the max nearby entities.
	 */
	public SpawnerBuilder setMaxNearbyEntities(int max) {
		tag.setShort(MAX_NEARBY_ENTITIES, (short) max);
		return this;
	}

	/**
	 * Sets the required player radius (in blocks) to activate.
	 */
	public SpawnerBuilder setPlayerRange(int range) {
		tag.setShort(REQUIRED_PLAYER_RANGE, (short) range);
		return this;
	}

	/**
	 * Sets the spawn radius (in blocks).
	 */
	public SpawnerBuilder setSpawnRange(int range) {
		tag.setShort(SPAWN_RANGE, (short) range);
		return this;
	}

	/**
	 * Sets the additional NBT data for the first mob spawned (or all, if potentials are not set).
	 * @param data An entity, written to NBT, in the format read by AnvilChunkLoader.readWorldEntity()
	 */
	public SpawnerBuilder setSpawnData(NBTTagCompound data) {
		if (data == null) {
			data = new NBTTagCompound();
			data.setString(ID, "minecraft:pig");
		}
		baseEntity.nbt = data.copy();
		return this;
	}

	/*
	 * Sets the list of entities the mob spawner will choose from.
	 */
	public SpawnerBuilder setPotentials(WeightedSpawnerEntity... entries) {
		hasPotentials = true;
		tag.setTag(SPAWN_POTENTIALS, new NBTTagList());
		NBTTagList list = tag.getTagList(SPAWN_POTENTIALS, 10);
		for (WeightedSpawnerEntity e : entries)
			list.appendTag(e.toCompoundTag());
		return this;
	}

	/*
	 * Adds to the list of entities the mob spawner will choose from.
	 */
	public SpawnerBuilder addPotentials(WeightedSpawnerEntity... entries) {
		hasPotentials = true;
		NBTTagList list = tag.getTagList(SPAWN_POTENTIALS, 10);
		for (WeightedSpawnerEntity e : entries)
			list.appendTag(e.toCompoundTag());
		return this;
	}

	/**
	 * @return The spawn data, represented as an entity nbt tag.
	 */
	public NBTTagCompound getSpawnData() {
		return tag.getCompoundTag(SPAWN_DATA);
	}

	/**
	 * @return The spawn data, represented as an entity nbt tag.
	 */
	public NBTTagList getPotentials() {
		return tag.getTagList(SPAWN_POTENTIALS, 10);
	}

	public TileEntityMobSpawner build(World world, BlockPos pos) {
		TileEntityMobSpawner s = (TileEntityMobSpawner) ((ITileEntityProvider) Blocks.MOB_SPAWNER).createNewTileEntity(null, 0);
		if (!hasPotentials) {
			NBTTagList list = new NBTTagList();
			list.appendTag(baseEntity.toCompoundTag());
			tag.setTag(SPAWN_POTENTIALS, list);
		}
		s.setWorld(world);
		s.setPos(pos);
		s.readFromNBT(tag);
		return s;
	}
}
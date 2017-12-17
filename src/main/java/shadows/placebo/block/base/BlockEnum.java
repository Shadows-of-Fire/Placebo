package shadows.placebo.block.base;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.placebo.client.RenamedStateMapper;
import shadows.placebo.interfaces.IPropertyEnum;
import shadows.placebo.itemblock.ItemBlockEnum;
import shadows.placebo.registry.RegistryInformation;
import shadows.placebo.util.PlaceboUtil;

public abstract class BlockEnum<E extends Enum<E> & IPropertyEnum> extends BlockBasic implements IEnumBlock<E> {

	protected final List<E> types = new ArrayList<E>();
	protected final Predicate<E> valueFilter;
	protected final PropertyEnum<E> property;
	protected final BlockStateContainer realStateContainer;

	public BlockEnum(String name, Material material, SoundType sound, float hardness, float resistance, Class<E> enumClass, String propName, Predicate<E> valueFilter, RegistryInformation info) {
		super(name, material, hardness, resistance, info);
		this.setSoundType(sound);
		this.valueFilter = valueFilter;
		this.property = PropertyEnum.create(propName, enumClass, valueFilter);
		types.addAll(property.getAllowedValues());
		this.realStateContainer = createStateContainer();
		this.setDefaultState(getBlockState().getBaseState());
		for (E e : types)
			e.set(this);
	}

	public BlockEnum(String name, Material material, SoundType sound, float hardness, float resistance, Class<E> enumClass, String propName, RegistryInformation info) {
		this(name, material, sound, hardness, resistance, enumClass, propName, Predicates.alwaysTrue(), info);
	}

	public BlockEnum(String name, Material material, SoundType sound, float hardness, float resistance, Class<E> enumClass, RegistryInformation info) {
		this(name, material, sound, hardness, resistance, enumClass, "type", info);
	}

	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockEnum<E>(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModels(ModelRegistryEvent e) {
		String modid = this.getRegistryName().getResourceDomain();
		for (int i = 0; i < types.size(); i++) {
			PlaceboUtil.sMRL("blocks", this, i, "type=" + types.get(i).getName());
		}
		ModelLoader.setCustomStateMapper(this, new RenamedStateMapper(modid, "blocks"));
	}

	@Override
	public final BlockStateContainer createBlockState() {
		return new BlockStateContainer.Builder(this).build(); // Blank to avoid crashes
	}

	@Override
	public final BlockStateContainer getBlockState() {
		return realStateContainer;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(property, types.get(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(property).ordinal();
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < this.types.size(); i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public BlockStateContainer createStateContainer() {
		return new BlockStateContainer(this, property);
	}

	@Override
	public List<E> getTypes() {
		return types;
	}

	@Override
	public PropertyEnum<E> getProperty() {
		return property;
	}

	@Override
	public BlockStateContainer getRealStateContainer() {
		return realStateContainer;
	}

	@Override
	public IBlockState getStateFor(E e) {
		return this.getDefaultState().withProperty(property, e);
	}

}

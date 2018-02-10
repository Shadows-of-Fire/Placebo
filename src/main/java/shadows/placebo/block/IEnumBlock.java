package shadows.placebo.block;

import java.util.List;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import shadows.placebo.interfaces.IItemBlock;
import shadows.placebo.interfaces.IPropertyEnum;
import shadows.placebo.interfaces.ISpecialPlacement;

public interface IEnumBlock<E extends Enum<E> & IPropertyEnum> extends IEnumBlockAccess<E>, ISpecialPlacement, IItemBlock {

	public List<E> getTypes();

	public BlockStateContainer createStateContainer();

	public BlockStateContainer getRealStateContainer();

	public PropertyEnum<E> getProperty();

	default public E getValue(IBlockState state) {
		return state.getValue(getProperty());
	}

}

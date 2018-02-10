package shadows.placebo.block;

import net.minecraft.block.state.IBlockState;
import shadows.placebo.interfaces.IPropertyEnum;

public interface IEnumBlockAccess<E extends IPropertyEnum> {

	IBlockState getStateFor(E e);

}

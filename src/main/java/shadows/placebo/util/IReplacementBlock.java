package shadows.placebo.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;

public interface IReplacementBlock {

	public void _setDefaultState(BlockState state);

	public void setStateContainer(StateContainer<Block, BlockState> container);

	/** Default Implementation (just copy this onto any implementing class)
	
	@Override
	public void _setDefaultState(BlockState state) {
		this.setDefaultState(state);
	}
	
	protected StateContainer<Block, BlockState> container;
	
	@Override
	public void setStateContainer(StateContainer<Block, BlockState> container) {
		this.container = container;
	}
	
	@Override
	public StateContainer<Block, BlockState> getStateContainer() {
		return container == null ? super.getStateContainer() : container;
	}
	
	*/
}

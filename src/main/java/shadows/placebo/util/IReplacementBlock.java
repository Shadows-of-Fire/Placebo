package shadows.placebo.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public interface IReplacementBlock {

    public void _setDefaultState(BlockState state);

    public void setStateContainer(StateDefinition<Block, BlockState> container);
}

/* Default Implementation (just copy this onto any implementing class)
 protected StateContainer<Block, BlockState> container;

 @Override
 public void _setDefaultState(BlockState state) {
     this.setDefaultState(state);
 }

 @Override
 public void setStateContainer(StateContainer<Block, BlockState> container) {
     this.container = container;
 }
 
 @Override
 public StateContainer<Block, BlockState> getStateContainer() {
     return container == null ? super.getStateContainer() : container;
 }
*/
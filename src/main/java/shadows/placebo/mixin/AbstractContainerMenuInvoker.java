package shadows.placebo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuInvoker {

    @Invoker("moveItemStackTo")
    public boolean _moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection);

}

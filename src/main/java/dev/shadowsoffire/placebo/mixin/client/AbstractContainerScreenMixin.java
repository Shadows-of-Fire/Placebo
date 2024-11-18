package dev.shadowsoffire.placebo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public class AbstractContainerScreenMixin implements DrawsOnLeft {

}

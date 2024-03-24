package dev.shadowsoffire.placebo.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.neoforged.neoforge.network.IContainerFactory;

public class MenuUtil {

    /**
     * Creates a {@link MenuType} with the target menu supplier and the vanilla feature flags.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> type(MenuSupplier<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }

    /**
     * Creates a {@link MenuType} with a forge {@link IContainerFactory} and the vanilla feature flags.
     * <p>
     * Necessary because the constructor of {@link MenuType} does not allow for lambda registration with {@link IContainerFactory}.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> bufType(IContainerFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }

    /**
     * Util method for the most common type of {@link IContainerFactory} - one supplying a {@link BlockPos}.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> posType(PosFactory<T> factory) {
        return new MenuType<>(factory(factory), FeatureFlags.DEFAULT_FLAGS);
    }

    /**
     * Helper method wrapping {@link NetworkHooks#openScreen} that returns {@link InteractionResult}.
     * Designed for use with {@link BlockEntityMenu}.
     */
    public static <M extends AbstractContainerMenu> InteractionResult openGui(Player player, BlockPos pos, PosFactory<M> factory) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        player.openMenu(new SimplerMenuProvider<>(player.level(), pos, factory), pos);
        return InteractionResult.CONSUME;
    }

    /**
     * Conversion helper that allows using {@link PosFactory} as a lambda to create an {@link IContainerFactory}.
     */
    public static <T extends AbstractContainerMenu> IContainerFactory<T> factory(PosFactory<T> factory) {
        return factory;
    }

    @FunctionalInterface
    public static interface PosFactory<T extends AbstractContainerMenu> extends IContainerFactory<T> {
        T create(int id, Inventory pInv, BlockPos pos);

        @Override
        default T create(int id, Inventory inv, FriendlyByteBuf buf) {
            return this.create(id, inv, buf.readBlockPos());
        }
    }

}

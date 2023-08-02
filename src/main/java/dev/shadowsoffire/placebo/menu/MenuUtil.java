package dev.shadowsoffire.placebo.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;

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
    public static <T extends AbstractContainerMenu> MenuType<T> type(IContainerFactory<T> factory) {
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
        NetworkHooks.openScreen((ServerPlayer) player, new SimplerMenuProvider<>(player.level(), pos, factory), pos);
        return InteractionResult.CONSUME;
    }

    /**
     * IIntArray can only send shorts, so we need to split int values in two.
     *
     * @param value The int to split
     * @param upper If sending the upper bits or not.
     * @return The appropriate half of the integer.
     */
    public static int split(int value, boolean upper) {
        return upper ? value >> 16 : value & 0xFFFF;
    }

    /**
     * IIntArray can only send shorts, so we need to split int values in two.
     *
     * @param current The current value
     * @param value   The split integer, recieved from network
     * @param upper   If receiving the upper bits or not.
     * @return The updated value.
     */
    public static int merge(int current, int value, boolean upper) {
        if (upper) {
            return current & 0x0000FFFF | value << 16;
        }
        else {
            return current & 0xFFFF0000 | value & 0x0000FFFF;
        }
    }

    public static <T extends AbstractContainerMenu> IContainerFactory<T> factory(PosFactory<T> factory) {
        return (id, inv, buf) -> factory.create(id, inv, buf.readBlockPos());
    }

    public static interface PosFactory<T> {
        T create(int id, Inventory pInv, BlockPos pos);
    }

}

package dev.shadowsoffire.placebo.menu;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Extension of {@link ResourceHandlerSlot} which takes a filter on what may enter the slot.
 */
public class FilteredSlot extends ResourceHandlerSlot {

    protected final InternalItemHandler handler;
    protected final Predicate<ItemStack> filter;
    protected final int index;

    /**
     * Creates a new filtered slot
     *
     * @param handler The backing item handler
     * @param index   The slot index
     * @param x       The x coordinate
     * @param y       The y coordinate
     * @param filter  A filter controlling what items may be placed in the slot by a player
     */
    public FilteredSlot(InternalItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
        super(handler, handler::set, index, x, y);
        this.handler = handler;
        this.filter = filter;
        this.index = index;
    }

    public FilteredSlot(InternalItemHandler handler, int index, int x, int y) {
        this(handler, index, x, y, Predicates.alwaysTrue());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.filter.test(stack);
    }

    /**
     * Overridden to bypass any restrictive {@code extract} overrides on {@link InternalItemHandler}
     * subclasses — the parent's default uses the public {@code extract}, which would fail for slots
     * the menu considers valid but that mod automation would reject.
     */
    @Override
    public boolean mayPickup(Player playerIn) {
        ItemResource resource = this.handler.getResource(this.index);
        if (resource.isEmpty()) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            return this.handler.extractInternal(this.index, resource, 1, tx) >= 1;
        }
    }

}

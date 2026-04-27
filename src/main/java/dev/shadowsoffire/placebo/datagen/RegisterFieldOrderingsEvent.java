package dev.shadowsoffire.placebo.datagen;

import org.jetbrains.annotations.ApiStatus;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired during datagen to allow mods to register {@link FieldOrderingFactory} instances.
 * <p>
 * Listeners can register factories that customize the JSON field ordering for objects written by
 * {@link net.minecraft.data.DataProvider#saveStable saveStable}. This is the preferred replacement for mutating
 * {@link net.minecraft.data.DataProvider#FIXED_ORDER_FIELDS} directly, since registered factories can be scoped by
 * output path or JSON content via {@link FilteredOrderingFactory}.
 * <p>
 * Fired on the mod-specific event bus, before any data providers run.
 */
public class RegisterFieldOrderingsEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public RegisterFieldOrderingsEvent() {}

    /**
     * Registers a {@link FieldOrderingFactory}. See {@link FieldOrderingFactory#forType} and
     * {@link FieldOrderingFactory#forSubtypedObject} for common factory builders.
     */
    public void register(FieldOrderingFactory factory) {
        FieldOrderingFactory.register(factory);
    }

}

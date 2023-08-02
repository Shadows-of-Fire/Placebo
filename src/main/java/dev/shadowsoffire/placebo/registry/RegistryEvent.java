package dev.shadowsoffire.placebo.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEvent<T> extends GenericEvent<T> implements IModBusEvent {

    RegistryEvent(Class<T> clazz) {
        super(clazz);
    }

    /**
     * Generic-type registry event reminiscent of days long past.
     * This is fired for the same set of registries that {@link RegObjHelper} has helpers for.
     */
    public static class Register<T> extends RegistryEvent<T> {
        private final IForgeRegistry<T> registry;
        private final ResourceLocation name;
        private final RegistryWrapper<T> wrapper;

        public Register(Class<T> clazz, IForgeRegistry<T> registry) {
            super(clazz);
            this.name = registry.getRegistryKey().location();
            this.registry = registry;
            this.wrapper = new RegistryWrapper<>(registry);
        }

        /**
         * Gets a registry wrapper that has a registerAll helper.
         */
        public RegistryWrapper<T> getRegistry() {
            return this.wrapper;
        }

        public IForgeRegistry<T> getForgeRegistry() {
            return this.registry;
        }

        public ResourceLocation getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "RegistryEvent.Register<" + this.getName() + ">";
        }
    }

    public static class RegistryWrapper<T> {
        private final IForgeRegistry<T> reg;

        public RegistryWrapper(IForgeRegistry<T> reg) {
            this.reg = reg;
        }

        /**
         * Registers a single object.
         *
         * @param object The object being registered
         * @param id     The ID of the object being registered. A modid will be filled in from context if absent.
         */
        public void register(T object, String id) {
            this.reg.register(id, object);
        }

        /**
         * Registers a single object.
         *
         * @param object The object being registered
         * @param id     The ID of the object being registered.
         */
        public void register(T object, ResourceLocation id) {
            this.reg.register(id, object);
        }

        /**
         * Registers multiple objects. Objects must be passed [object, id] repeating.
         * This method does not validate that an ID is present, and will crash if one is not.
         *
         * @param arr The vararg array of objects and ids.
         */
        @SuppressWarnings("unchecked")
        public void registerAll(Object... arr) {
            for (int i = 0; i < arr.length; i += 2) {
                T object = (T) arr[i];
                Object id = arr[i + 1];
                if (id instanceof String s) this.reg.register(s, object);
                else if (id instanceof ResourceLocation r) this.reg.register(r, object);
                else throw new RuntimeException();
            }
        }
    }
}

package dev.shadowsoffire.placebo.reload;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;

/**
 * A Listener Callback is something that reacts to the loading stages of {@link DynamicRegistry}.
 *
 * @param <R> The type of the reload listener.
 */
public interface RegistryCallback<R extends TypeKeyed & PSerializable<? super R>> {

    /**
     * Called when the manager begins reloading, before the registry has been cleared.
     *
     * @param manager The manager that is reloading.
     */
    public void beginReload(DynamicRegistry<R> manager);

    /**
     * Called when the manager has finished reloading, and the registry is frozen.
     *
     * @param manager The manager that is reloading.
     */
    public void onReload(DynamicRegistry<R> manager);

    /**
     * Creates a {@link RegistryCallback} out of two consumers.
     *
     * @param <R>         The type of the manager.
     * @param beginReload The consumer to run on reload start.
     * @param onReload    The consumer to run on reload completion.
     * @return A ListenerCallback composing the two consumers.
     */
    public static <R extends TypeKeyed & PSerializable<? super R>> RegistryCallback<R> create(Consumer<DynamicRegistry<R>> beginReload, Consumer<DynamicRegistry<R>> onReload) {
        return new Delegated<>(beginReload, onReload);
    }

    /**
     * Creates a {@link RegistryCallback} that only runs on reload start.
     *
     * @param <R>         The type of the manager.
     * @param beginReload The consumer to run on reload start.
     * @return A ListenerCallback that will run the consumer on reload start.
     */
    public static <R extends TypeKeyed & PSerializable<? super R>> RegistryCallback<R> beginOnly(Consumer<DynamicRegistry<R>> beginReload) {
        return new Delegated<>(beginReload, v -> {});
    }

    /**
     * Creates a {@link RegistryCallback} that only runs on reload completion.
     *
     * @param <R>      The type of the manager.
     * @param onReload The consumer to run on reload completion.
     * @return A ListenerCallback that will run the consumer on reload completion.
     */
    public static <R extends TypeKeyed & PSerializable<? super R>> RegistryCallback<R> reloadOnly(Consumer<DynamicRegistry<R>> onReload) {
        return new Delegated<>(v -> {}, onReload);
    }

    @ApiStatus.Internal
    class Delegated<R extends TypeKeyed & PSerializable<? super R>> implements RegistryCallback<R> {

        private Consumer<DynamicRegistry<R>> beginReload, onReload;

        public Delegated(Consumer<DynamicRegistry<R>> beginReload, Consumer<DynamicRegistry<R>> onReload) {
            this.beginReload = beginReload;
            this.onReload = onReload;
        }

        @Override
        public void beginReload(DynamicRegistry<R> manager) {
            this.beginReload.accept(manager);
        }

        @Override
        public void onReload(DynamicRegistry<R> manager) {
            this.onReload.accept(manager);
        }

    }

}

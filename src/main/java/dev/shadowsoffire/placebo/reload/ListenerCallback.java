package dev.shadowsoffire.placebo.reload;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;

/**
 * A Listener Callback is something that reacts to the loading stages of {@link PlaceboJsonReloadListener}.
 *
 * @param <V> The type of the reload listener.
 */
public interface ListenerCallback<V extends TypeKeyed & PSerializable<? super V>> {

    /**
     * Called when the manager begins reloading, before the registry has been cleared.
     *
     * @param manager The manager that is reloading.
     */
    public void beginReload(PlaceboJsonReloadListener<V> manager);

    /**
     * Called when the manager has finished reloading, and the registry is frozen.
     *
     * @param manager The manager that is reloading.
     */
    public void onReload(PlaceboJsonReloadListener<V> manager);

    /**
     * Creates a {@link ListenerCallback} out of two consumers.
     *
     * @param <V>         The type of the manager.
     * @param beginReload The consumer to run on reload start.
     * @param onReload    The consumer to run on reload completion.
     * @return A ListenerCallback composing the two consumers.
     */
    public static <V extends TypeKeyed & PSerializable<? super V>> ListenerCallback<V> create(Consumer<PlaceboJsonReloadListener<V>> beginReload, Consumer<PlaceboJsonReloadListener<V>> onReload) {
        return new Delegated<>(beginReload, onReload);
    }

    /**
     * Creates a {@link ListenerCallback} that only runs on reload start.
     *
     * @param <V>         The type of the manager.
     * @param beginReload The consumer to run on reload start.
     * @return A ListenerCallback that will run the consumer on reload start.
     */
    public static <V extends TypeKeyed & PSerializable<? super V>> ListenerCallback<V> beginOnly(Consumer<PlaceboJsonReloadListener<V>> beginReload) {
        return new Delegated<>(beginReload, v -> {});
    }

    /**
     * Creates a {@link ListenerCallback} that only runs on reload completion.
     *
     * @param <V>      The type of the manager.
     * @param onReload The consumer to run on reload completion.
     * @return A ListenerCallback that will run the consumer on reload completion.
     */
    public static <V extends TypeKeyed & PSerializable<? super V>> ListenerCallback<V> reloadOnly(Consumer<PlaceboJsonReloadListener<V>> onReload) {
        return new Delegated<>(v -> {}, onReload);
    }

    @ApiStatus.Internal
    class Delegated<V extends TypeKeyed & PSerializable<? super V>> implements ListenerCallback<V> {

        private Consumer<PlaceboJsonReloadListener<V>> beginReload, onReload;

        public Delegated(Consumer<PlaceboJsonReloadListener<V>> beginReload, Consumer<PlaceboJsonReloadListener<V>> onReload) {
            this.beginReload = beginReload;
            this.onReload = onReload;
        }

        @Override
        public void beginReload(PlaceboJsonReloadListener<V> manager) {
            this.beginReload.accept(manager);
        }

        @Override
        public void onReload(PlaceboJsonReloadListener<V> manager) {
            this.onReload.accept(manager);
        }

    }

}

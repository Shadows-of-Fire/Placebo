package shadows.placebo.json;

import java.util.function.Consumer;

public interface ListenerCallback<V extends TypeKeyed<V>> {

	public void beginReload(PlaceboJsonReloadListener<V> manager);

	public void onReload(PlaceboJsonReloadListener<V> manager);

	public static class Delegated<V extends TypeKeyed<V>> implements ListenerCallback<V> {

		private Consumer<PlaceboJsonReloadListener<V>> beginReload, onReload;

		public Delegated(Consumer<PlaceboJsonReloadListener<V>> beginReload, Consumer<PlaceboJsonReloadListener<V>> onReload) {
			this.beginReload = beginReload;
			this.onReload = onReload;
		}

		@Override
		public void beginReload(PlaceboJsonReloadListener<V> manager) {
			beginReload.accept(manager);
		}

		@Override
		public void onReload(PlaceboJsonReloadListener<V> manager) {
			onReload.accept(manager);
		}

	}

	public static <V extends TypeKeyed<V>> ListenerCallback<V> create(Consumer<PlaceboJsonReloadListener<V>> beginReload, Consumer<PlaceboJsonReloadListener<V>> onReload) {
		return new Delegated<>(beginReload, onReload);
	}

	public static <V extends TypeKeyed<V>> ListenerCallback<V> beginOnly(Consumer<PlaceboJsonReloadListener<V>> beginReload) {
		return new Delegated<>(beginReload, v -> {
		});
	}

	public static <V extends TypeKeyed<V>> ListenerCallback<V> reloadOnly(Consumer<PlaceboJsonReloadListener<V>> onReload) {
		return new Delegated<>(v -> {
		}, onReload);
	}

}

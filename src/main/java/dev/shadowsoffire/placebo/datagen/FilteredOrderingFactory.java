package dev.shadowsoffire.placebo.datagen;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.CommonHooks;

public record FilteredOrderingFactory(Predicate<ParsedPath> pathFilter, Predicate<JsonElement> jsonFilter, Comparator<String> comparator) implements FieldOrderingFactory {

    @Override
    public Comparator<String> getKeyComparator(JsonElement json, Path path) {
        ParsedPath parsed = ParsedPath.parse(path);
        if (parsed != null && this.pathFilter.test(parsed) && this.jsonFilter.test(json)) {
            return this.comparator;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable // "Total" filter, if the user provides one.
        private Predicate<ParsedPath> pathFilter = null;
        private Predicate<JsonElement> jsonFilter = j -> true;
        private Comparator<String> comparator;
        // Fields for composing filters. These three things compose via logical-AND together, but logical-OR within their own categories.
        private Optional<PackType> packType = Optional.empty();
        private Set<String> namespaces = new HashSet<>();
        private Set<String> prefixes = new HashSet<>();

        /**
         * Makes this ordering only apply to objects in the specified pack type.
         */
        public Builder packType(PackType type) {
            this.packType = Optional.of(type);
            return this;
        }

        /**
         * Makes this ordering only apply to objects generated in the target namespace(s).
         */
        public Builder namespaces(String... namespaces) {
            for (String s : namespaces) {
                this.namespaces.add(s);
            }
            return this;
        }

        /**
         * Makes this ordering only apply to objects generated in the target registry.
         */
        public Builder registries(Identifier... registryKeys) {
            for (Identifier id : registryKeys) {
                this.prefixes.add(CommonHooks.prefixNamespace(id) + "/");
            }
            return this;
        }

        /**
         * Applies a JSON filter that checks if the JSON object has a field with the given key and string value.
         * <p>
         * This can be used to filter by object subtype, e.g. "crafting_shaped" for recipes.
         */
        public Builder objectSubtype(String typeKey, String subtype) {
            return jsonFilter(typeKey, new JsonPrimitive(subtype));
        }

        /**
         * Version of {@link #forObjectSubtype(String, String)} that uses "type" as the key.
         */
        public Builder objectSubtype(String subtype) {
            return objectSubtype("type", subtype);
        }

        /**
         * Applies an ordering based on the given field names, in the given order.
         *
         * @apiNote This method inherits the default mappings from {@link DataProvider#FIXED_ORDER_FIELDS}.
         */
        public Builder order(String... fieldsInOrder) {
            Object2IntOpenHashMap<String> map = defaultMap();
            for (int i = 0; i < fieldsInOrder.length; i++) {
                map.put(fieldsInOrder[i], 2 + i); // Use this offset of 2 to avoid clashing with the default settings.
            }
            map.defaultReturnValue(2 + fieldsInOrder.length); // Fields not in the list get the next index after the last one.
            Comparator<String> comparator = Comparator.comparingInt(map).thenComparing(Function.identity());
            return comparator(comparator);
        }

        /**
         * Applies an ordering based on a modified copy of {@link DataProvider#FIXED_ORDER_FIELDS}.
         * <p>
         * Note that unless set, the map's default return value is 2.
         */
        public Builder orderMap(Consumer<Object2IntOpenHashMap<String>> orderBuilder) {
            Object2IntOpenHashMap<String> map = defaultMap();
            orderBuilder.accept(map);
            Comparator<String> comparator = Comparator.comparingInt(map).thenComparing(Function.identity());
            return comparator(comparator);
        }

        /**
         * Applies a raw path filter.
         * It is illegal to call this method in conjunction with any of {@link #packType(PackType)}, {@link #namespaces(String...)}, or
         * {@link #registries(Identifier...)}.
         */
        public Builder pathFilter(Predicate<ParsedPath> filter) {
            this.pathFilter = filter;
            return this;
        }

        /**
         * Applies a JSON filter that checks if the JSON object has a field with the given key and value.
         */
        public <T> Builder jsonFilter(String key, JsonElement value) {
            return jsonFilter(j -> j.isJsonObject() && value.equals(j.getAsJsonObject().get(key)));
        }

        public Builder jsonFilter(Predicate<JsonElement> filter) {
            this.jsonFilter = filter;
            return this;
        }

        public Builder comparator(Comparator<String> comparator) {
            this.comparator = comparator;
            return this;
        }

        public FilteredOrderingFactory build() {
            if (this.comparator == null) {
                throw new IllegalStateException("Comparator must be set");
            }
            Predicate<ParsedPath> pathFilter = this.pathFilter;
            if (this.pathFilter != null) {
                Preconditions.checkArgument(this.packType.isEmpty() && this.namespaces.isEmpty() && this.prefixes.isEmpty(), "pathFilter may not be called with any of packType/namespaces/registries as it overrides them.");
            }
            else {
                if (this.packType.isPresent()) {
                    pathFilter = optionallyCompose(pathFilter, p -> p.type() == this.packType.get());
                }
                if (!this.namespaces.isEmpty()) {
                    pathFilter = optionallyCompose(pathFilter, p -> this.namespaces.contains(p.namespace));
                }
                if (!this.prefixes.isEmpty()) {
                    pathFilter = optionallyCompose(pathFilter, p -> {
                        for (String prefix : this.prefixes) {
                            if (p.path.startsWith(prefix)) return true;
                        }
                        return false;
                    });
                }
                Preconditions.checkArgument(pathFilter != null, "A path filter must be provided, either by pathFilter or a combination of packType/namespaces/registries");
            }
            return new FilteredOrderingFactory(pathFilter, this.jsonFilter, this.comparator);
        }

        private static Object2IntOpenHashMap<String> defaultMap() {
            Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>((Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS);
            map.defaultReturnValue(2); // This is the default return value for FIXED_ORDER_FIELDS, but the copy constructor doesn't copy it.
            return map;
        }

        private static Predicate<ParsedPath> optionallyCompose(@Nullable Predicate<ParsedPath> p1, Predicate<ParsedPath> p2) {
            return p1 != null ? p1.and(p2) : p2;
        }

    }

    public record ParsedPath(PackType type, String namespace, String path) {

        private static final Set<String> VALID_PACK_TYPES = Set.of("assets", "data");

        /**
         * Reverse engineers a datagen path into its components by stripping the captured pack output
         * root (see {@link FieldOrderingFactory.Impl#getPackRoot}) and decomposing the remainder.
         * <p>
         * The output layout depends on the {@code flat} flag passed to
         * {@link net.neoforged.neoforge.data.loading.DatagenModLoader#begin DatagenModLoader.begin}:
         * <ul>
         * <li>Non-flat (default): {@code <root>/<modid>/<packtype>/<namespace>/<elementsPath>/<file>.json}</li>
         * <li>Flat: {@code <root>/<packtype>/<namespace>/<elementsPath>/<file>.json}</li>
         * </ul>
         * Returns {@code null} if the path is not under the known root or doesn't match either layout.
         */
        @Nullable
        public static ParsedPath parse(Path absPath) {
            Path root = FieldOrderingFactory.Impl.getPackRoot();
            if (root == null) {
                return null;
            }
            Path normalized = absPath.toAbsolutePath().normalize();
            if (!normalized.startsWith(root)) {
                return null;
            }
            Path rel = root.relativize(normalized);
            int nameCount = rel.getNameCount();
            if (nameCount < 3) {
                return null;
            }
            // Skip the per-mod directory if present (non-flat layout).
            int packTypeIdx = 0;
            if (!VALID_PACK_TYPES.contains(rel.getName(0).toString())) {
                packTypeIdx = 1;
                if (nameCount - packTypeIdx < 3) {
                    return null;
                }
            }
            String packType = rel.getName(packTypeIdx).toString();
            if (!VALID_PACK_TYPES.contains(packType)) {
                return null;
            }
            String namespace = rel.getName(packTypeIdx + 1).toString();
            String path = rel.subpath(packTypeIdx + 2, nameCount).toString().replace(File.separatorChar, '/');
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - ".json".length());
            }
            return new ParsedPath(packType.equals("assets") ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA, namespace, path);
        }
    }

}

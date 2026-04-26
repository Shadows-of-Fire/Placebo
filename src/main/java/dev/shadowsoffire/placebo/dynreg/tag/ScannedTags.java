package dev.shadowsoffire.placebo.dynreg.tag;

import java.util.List;
import java.util.Map;

import dev.shadowsoffire.placebo.dynreg.tag.TagLoader.EntryWithSource;
import net.minecraft.resources.Identifier;

/**
 * Pairs a {@link TagLoader} with its scan output so the apply phase of
 * {@link DynamicTagManager} can run resolution against the now-populated registry content.
 * <p>
 * Top-level rather than nested in {@link DynamicTagManager} because Java forbids referencing a nested type
 * from its enclosing class's supertype declaration.
 */
record ScannedTags<R>(TagLoader<R> loader, Map<Identifier, List<EntryWithSource>> raw) {

    Map<Identifier, List<Identifier>> resolve() {
        return this.loader.resolve(this.raw);
    }
}

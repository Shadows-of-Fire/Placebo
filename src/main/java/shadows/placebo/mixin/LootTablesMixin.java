package shadows.placebo.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import shadows.placebo.Placebo;
import shadows.placebo.loot.LootSystem;

@Mixin(LootTables.class)
public class LootTablesMixin {

	@Shadow
	public Map<ResourceLocation, LootTable> tables;

	@Inject(method = "apply", at = @At(value = "TAIL"))
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		tables = new HashMap<>(tables);
		LootSystem.PLACEBO_TABLES.forEach((key, val) -> {
			if (!tables.containsKey(key)) tables.put(key, val);
		});
		Placebo.LOGGER.info("Registered {} additional loot tables.", LootSystem.PLACEBO_TABLES.keySet().size());
	}
}

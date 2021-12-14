package shadows.placebo.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "TAIL"))
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler, CallbackInfo ci) {
		this.tables = new HashMap<>(this.tables);
		LootSystem.PLACEBO_TABLES.forEach((key, val) -> {
			if (!this.tables.containsKey(key)) this.tables.put(key, val);
		});
		Placebo.LOGGER.info("Registered {} additional loot tables.", LootSystem.PLACEBO_TABLES.keySet().size());
	}
}

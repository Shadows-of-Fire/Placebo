package shadows.placebo.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementTreeNode;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.command.FunctionObject.CacheableFunction;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadows.placebo.Placebo;

public class AdvancementHelper {

	//If this class causes absolutely anything bad to happen, go ping @Tamaized#7311 on the Modded Minecraft discord server.  All his fault.

	public static final AdvancementList ADVANCEMENTS = ReflectionHelper.getPrivateValue(AdvancementManager.class, null, "ADVANCEMENT_LIST", "field_192784_c");
	private static final Map<ResourceLocation, Builder> TO_REGISTER = new HashMap<>();
	private static Constructor<Builder> ctr = null;

	public static void preInit(FMLPreInitializationEvent e) {
		reload();
		MinecraftForge.EVENT_BUS.register(new AdvancementHelper());
	}

	public static void addAdvancements() {
		new AdvancementBuilderBuilder(Placebo.MODID, "test").setDisplayInfo(createDisplay("test.title", "test.desc", new ItemStack(Items.APPLE), null, FrameType.TASK, true, true, false)).setRewards(createRewards(50F)).setParent("adventure/root").addCriteria("test_crit", new KilledTrigger.Instance(CriteriaTriggers.PLAYER_KILLED_ENTITY.getId(), EntityPredicate.ANY, DamageSourcePredicate.ANY)).addConditionGroup("test_crit").build().register();
	}

	@SubscribeEvent
	public void worldServerCaps(AttachCapabilitiesEvent<World> e) {
		World world = e.getObject();
		ReflectionHelper.setPrivateValue(World.class, world, new HackedAdvancementManager(new File(new File(world.getSaveHandler().getWorldDirectory(), "data"), "advancements")), "advancementManager", "field_191951_C");
	}

	public static void reload() {
		TO_REGISTER.clear();
		addAdvancements();
		ADVANCEMENTS.loadAdvancements(TO_REGISTER);
		for (Advancement advancement : ADVANCEMENTS.getRoots()) {
			if (advancement.getDisplay() != null) {
				AdvancementTreeNode.layout(advancement);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Builder genBuilder(@Nullable ResourceLocation parent, @Nullable DisplayInfo info, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements) {
		if (ctr == null) try {
			ctr = (Constructor<Builder>) Builder.class.getDeclaredConstructors()[0];
			ctr.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Failed to access Advancement.Builder constructor!");
		}

		try {
			return ctr.newInstance(parent, info, rewards, criteria, requirements);
		} catch (Exception e) {
			throw new RuntimeException("Failed instantiate Advancement.Builder!");
		}
	}

	public static DisplayInfo createDisplay(String langKey, String descKey, ItemStack stack, String background, FrameType type, boolean toast, boolean announce, boolean hidden) {
		return new DisplayInfo(stack, new TextComponentTranslation(langKey), new TextComponentTranslation(descKey), background == null || background.isEmpty() ? null : new ResourceLocation(background), type, toast, announce, hidden);
	}

	public static AdvancementRewards createRewards(float experience, String[] lootTables, String[] recipeNames, CacheableFunction func) {
		ResourceLocation[] tables = new ResourceLocation[lootTables.length];
		ResourceLocation[] recipes = new ResourceLocation[recipeNames.length];

		for (int i = 0; i < lootTables.length; i++)
			tables[i] = new ResourceLocation(lootTables[i]);

		for (int i = 0; i < recipeNames.length; i++)
			recipes[i] = new ResourceLocation(recipeNames[i]);

		return new AdvancementRewards(0, tables, recipes, func);
	}

	public static AdvancementRewards createRewards(float experience, String[] lootTables, String[] recipeNames) {
		return createRewards(experience, lootTables, recipeNames, CacheableFunction.EMPTY);
	}

	public static AdvancementRewards createRewards(float experience, String[] lootTables) {
		return createRewards(experience, lootTables, new String[0]);
	}

	public static AdvancementRewards createRewards(float experience) {
		return createRewards(experience, new String[0]);
	}

	public static void registerAdvancement(ResourceLocation name, Builder b) {
		if (TO_REGISTER.get(name) == null) TO_REGISTER.put(name, b);
		else throw new RuntimeException("Attempted to register duplicate advancement! Name: " + name.toString());
	}

	public static class AdvancementBuilderBuilder {

		private DisplayInfo info;
		private AdvancementRewards rewards;
		private Map<String, Criterion> criteria = new HashMap<>();
		private ResourceLocation parent;

		private final List<String[]> REQUIRED = new ArrayList<>();
		private final ResourceLocation name;

		public AdvancementBuilderBuilder(ResourceLocation name) {
			this.name = name;
		}

		public AdvancementBuilderBuilder(String modid, String name) {
			this(new ResourceLocation(modid, name));
		}

		public AdvancementBuilderBuilder setDisplayInfo(DisplayInfo info) {
			if (this.info != null) throw new RuntimeException("Tried to set display info when it was already set!");
			this.info = info;
			return this;
		}

		public AdvancementBuilderBuilder addCriteria(String name, ICriterionInstance criterion) {
			criteria.put(name, new Criterion(criterion));
			return this;
		}

		public AdvancementBuilderBuilder setRewards(AdvancementRewards rewards) {
			if (this.rewards != null) throw new RuntimeException("Tried to set rewards when it was already set!");
			this.rewards = rewards;
			return this;
		}

		public AdvancementBuilderBuilder setParent(String advancement) {
			this.parent = new ResourceLocation(advancement);
			return this;
		}

		/**
		 * Sets an OR condition, as provided by the given criteria names.  If any 1 criteria from all groups is met, the advancement will be granted.
		 * @param required Criteria names, the same ones set in addCriteria.
		 */
		public AdvancementBuilderBuilder addConditionGroup(String... group) {
			REQUIRED.add(group);
			return this;
		}

		public BuilderResult build() {

			String[][] reqs = new String[REQUIRED.size()][];

			for (int i = 0; i < REQUIRED.size(); i++)
				reqs[i] = REQUIRED.get(i);

			return new BuilderResult(name, genBuilder(parent, info, rewards, criteria, reqs));
		}
	}

	public static class BuilderResult {
		public final Builder builder;
		public final ResourceLocation name;

		public BuilderResult(ResourceLocation name, Builder builder) {
			this.name = name;
			this.builder = builder;
		}

		public void register() {
			registerAdvancement(name, builder);
		}
	}

	public static class HackedAdvancementManager extends AdvancementManager {

		public HackedAdvancementManager(File advancementsDirIn) {
			super(advancementsDirIn);
		}

		@Override
		public void reload() {
			super.reload();
			AdvancementHelper.reload();
		}

	}

}

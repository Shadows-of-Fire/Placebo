## 8.0.0
* Updated to 1.20.1

## 7.2.1
* Added a method to invalidate all cached objects of a given type.

## 7.2.0
* Updated TypeKeyed to no longer require setting the serializer. Objects should be aware of what their serializer is, as two serializers should not produce the same object.
  * Breaking change for Apotheosis, Gateways, and HNN, so all of those will need to be updated as well.
  * Also improved some validation in the deserialization process.
* Added more codec support across the board.

## 7.1.8
* Made the TagParser accept codec-form CompoundTag (NBT) Objects.
* Fixed a crash that could happen if negative luck was encountered.
* Added new Supporter Wings (Armored, Blaze, Bone, Cloud, Fairy, Mechanical, and Monarch).
* Added CachedObject, which allows for caching of nbt-encoded itemstack objects.

## 7.1.7
* Fixed a network encode error in ItemAdapter.CODEC.

## 7.1.6
* More codec things.

## 7.1.5
* Changed StepFunction.CODEC to accept constant float values as well as the usual definition.
* Added the GetEnchantmentLevelEvent, which allows changing enchantment levels for gameplay purposes.
* Fixed ItemUseEvent not accepting null players as specified by UseOnContext.

## 7.1.4
* Added a hook for Placebo Container Menus to react to successful shift-click operations.

## 7.1.3
* Added Codec support to PSerializer.

## 7.1.2
* Fixed Optional-related crashes.

## 7.1.1
* Added the ability to access the max and current steps of a StepFunction.
* StepFunction is now a final class.

## 7.1.0
* Removed DimWeightedJsonReloadListener in favor of better logic in WeightedJsonReloadListener.
* Changed .cfg files such that they preserve insertion order for entries.

## 7.0.1
* Added everything needed to port Apotheosis.

## 7.0.0
* Updated to 1.19.2

## 6.6.6
* Added some additional json utility code.

## 6.6.5
* Added a variant of blit() that takes a color arg to ClientUtil.
* Fixed the rainbow color breaking on servers.
* Added boilerplate for TOP entity additions.
* Added strict error logging to RecipeHelper.
* Made RecipeHelper accept TagKey as an input type.
* Added more setters to ModifiableEnergyStorage.

## 6.6.4
* Added the AnvilLandEvent.
* Added a very angry logging message if a TickingEntityBlock has the wrong entity type class.

## 6.6.3
* Fixed a crash when a dimensional reload listener has no items for that dimension.

## 6.6.2
* Added DimWeightedJsonReloadListener for dimension-restricted weighted items.
* Pulled in StepFunction from Apotheosis.

## 6.6.1
* Fixed deserialized StackLootEntries not passing in conditions or functions.

## 6.6.0
* Added a sketchy reflection-based auto-registration method for SerializerBuilder.
  * It coerces read(FriendlyByteBuf), read(JsonObject), write(FriendlyByteBuf) and JsonObject write() from the source class as a serializer.

## 6.5.3
* Added WeightedJsonReloadListener to better handle weighted entries.

## 6.5.2
* Re-added the ItemUseEvent needed by Apotheosis.
  * Forge declined to add this event, so here it will stay.

## 6.5.1
* Made RandomAttributeModifier not crash on fail due to how it is used in Gateways.
  * Fixes GatewaysToEternity#3

## 6.5.0
* Rewrite to RecipeHelper to allow recipes to be regenerated on reload, rather than built once.
  * This means that configs that adjust recipes can work without restarting the game.
  * However, due to timing, it is likely they will need a double-reload to trigger.
* Added various utilities needed by Gateways to Eternity 2.0.0.
* Fixed a bug where Placebo-JSON reload listeners were syncing data back to the host on SSP/LAN
  * This caused data to be lost when only part of the object is sent to the client for any reason.

## 6.4.2
* Made changes to how energy syncs in Placebo Menus.
  * This should fix energy values going negative, I hope...

## 6.4.1
* Fixed recipe types not being registered.

## 6.4.0
* Minimum Forge Version bumped to 40.1.14.
  * Needed to access IContext from AddReloadListenerEvent
* Fixed PlaceboJsonReloadListener(s) failing to sync on the first login to a server.
  * This change is breaking, and mods using PJRL from 6.3.0 will not work with this build.
  * All impacted mods will have corresponding updates.
* Added new functionality to Configuration to support top-level comments, as well as a title.
  * A document detailing the file specification will also be linked to.
* Made QuickMoveHandler call setChanged on the clicked slot for shift-clicks.

## 6.3.0
* Fixed PlaceboJsonReloadListeners not checking IConditions.

## 6.2.1
* Fixed RecipeHelper not being invoked.

## 6.2.0
* Updated to 1.18.2 - This was a breaking change due to tag updates within MC.
* Fixed Rainbow Color.
* Added a new utility to register RecipeTypes since the old method is defunct.

## 6.1.1
* Added support for loading conditions on ALL placebo-based json reload listeners.
  * This includes Hostile Neural Networks, Apotheosis, and any other future projects.

## 6.1.0
* Added the PlaceboJsonReloadListener, which assists in creation of automatically synced data-driven systems.
* Rewrote EasyContainerData and PlaceboContainerMenu to allow data slots to have separate real and synced values.
  * Breaking Change
* Added the base code for TOP compat.

## 6.0.5
* Added util to check if LCTRL or RCTRL is down.

## 6.0.4
* Sushomeister: Added Ukranian and Russian translations.
* Hav3funDJ: Added Italian translation.
* Added util to add lore to an itemstack.
* Added the PlaceboTaskQueue to enqueue tick-based tasks.

## 6.0.3
* Added the ability to handle optional stacks in ItemAdapter.

## 6.0.2
* Fixed a bug where mods using QuickMoveHandler were crashing outside of dev.

## 6.0.1
* Fully updated to 1.18.1

## 6.0.0
* Barebones port of what is necessary for PackMenu (The config system).

## 5.0.3
* Added util to add the commonly-used inventory shuffle movement rules.

## 5.0.2
* Added a ton more container util code.

## 5.0.1
* Full port to 1.17.1
* Added some useful Container, TileEntity, and GUI Utils.
* Moved most JS Coremods to Mixin where possible.
* Stripped new events and PR'd to forge.
* Removed deprecated code.

## 5.0.0
* Barebones port of what is necessary for PackMenu (The config system).

## 4.6.1
* Placebo loot tables will now properly have their ID set.
* Added a /placebo command
* Added a subcommand to allow for serialization of existing loot tables.
* Cleaned up some code that needed.

## 4.6.0
* Code has been updated to Official (Mojang) Mappings.
* TagBuilder and ReflectionHelper have been deprecated and are pending removal.
# Description
An ItemStack is the representation of an item in Minecraft. The parser provided by Placebo is "complete", in that it
can accept all components of a stack, which most vanilla parsers cannot do.

# Dependencies
This object references the following objects:
1. [CompoundTag](./CompoundTag.md)

# Schema
```js
{
    "item": "string",       // [Mandatory] || Registry Name of item to load.
    "optional": boolean,    // [Optional, defaults to false] || If False, then the parser will error if the item is not located in the registry.
    "count": integer,       // [Optional, defaults to 1] || Stack Size
    "nbt": CompoundTag,     // [Optional] || Vanilla Item NBT
    "cap_nbt": CompoundTag, // [Optional] || Forge Item Capability NBT
}
```

# Examples
A full durability netherite sword
```json
{
    "item": "minecraft:netherite_sword",
    "count": 1,
    "nbt": {
        "Damage": 0,
        "Enchantments": [
            {
                "lvl": 4,
                "id": "minecraft:sharpness"
            }
        ]
    }
}
```

# Misc
Placebo provides the following commands to help working with itemstacks:
1. `/placebo hand` - Emits the currently held mainhand item as JSON in this format.
# Description
An ItemStack is the representation of an item in Minecraft. The parser provided by Placebo is "complete", in that it
can accept all components of a stack, which most vanilla parsers cannot do.

# Dependencies
This object references the following objects:
1. [CompoundTag](./CompoundTag.md)

# Schema
```js
{
    "item": "string",       // [Mandatory] || Registry name of the item to load.
    "optional": boolean,    // [Optional]  || If this stack is optional, and will produce an empty stack instead of throwing an error when the item is not found. Default value = false.
    "count": integer,       // [Optional]  || Stack Size. Default value = 1.
    "nbt": CompoundTag,     // [Optional]  || Vanilla Item NBT. Default value = empty NBT.
    "cap_nbt": CompoundTag, // [Optional]  || Forge Item Capability NBT. Default value = empty NBT.
}
```

Even if a stack is non-optional, specifying `"minecraft:air"` as the `"item"` will always produce an empty stack.  
Some consumers may not accept empty stacks for any reason.

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
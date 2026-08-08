# Description
An ItemStack is the representation of an item in Minecraft. The parser provided by Placebo is "complete", in that it
can accept all components of a stack, which most vanilla parsers cannot do.

# Schema
```js
{
    "id": "string",                   // [Mandatory] || Registry name of the item to load.
    "optional": boolean,              // [Optional]  || If this stack is optional, and will produce an empty stack instead of throwing an error when the item is not found. Default value = false.
    "count": integer,                 // [Optional]  || Stack Size. Default value = 1. Range: [1, 99].
    "components": DataComponentPatch  // [Optional]  || The data components of the stack. Default value = no components.
}
```

Even if a stack is non-optional, specifying `"minecraft:air"` as the `"id"` will always produce an empty stack.  
Some consumers may not accept empty stacks for any reason.

# Examples
A full durability netherite sword
```json
{
    "id": "minecraft:netherite_sword",
    "count": 1,
    "components": {
        "minecraft:enchantments": {
            "levels": {
                "minecraft:sharpness": 2
            }
        }
    }
}
```

# Misc
Placebo provides the following commands to help working with itemstacks:
1. `/placebo hand` - Emits the currently held mainhand item as JSON in this format.
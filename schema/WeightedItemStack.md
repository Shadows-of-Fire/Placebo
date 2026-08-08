# Description
A Weighted Item Stack is the combination of an ItemStack, a weight, and a drop chance (used when a weighted stack is equipped by an entity).  
They are used when an object must randomly select between different stacks.

# Dependencies
This object references the following objects:
1. [ItemStack](./ItemStack.md)

# Schema
```js
{
    "stack": ItemStack,   // [Mandatory] || The stack being provided.
    "weight": integer,    // [Mandatory] || Weight (relative to other weighted entries in the same list) of this object.
    "drop_chance": float  // [Optional]  || Drop chance of this stack, if used in a context where it will be equipped by an entity. Default value = -1.
}
```

Note: A negative drop chance leaves the entity's default drop chance for the slot unchanged.

# Examples
A netherite sword with Sharpness IV, with a weight of 10 and a guaranteed drop chance.
```json
{
    "weight": 10,
    "drop_chance": 2.0,
    "stack": {
        "id": "minecraft:netherite_sword",
        "count": 1,
        "components": {
            "minecraft:enchantments": {
                "levels": {
                    "minecraft:sharpness": 4
                }
            }
        }
    }
}
```
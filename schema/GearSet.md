# Description
A GearSet is a weighted list of equipment for an entity. It provides a list of [WeightedItemStacks](./WeightedItemStack.md) for each equipment slot.

# Dependencies
This object references the following objects:
1. [WeightedItemStacks](./WeightedItemStack.md)

# Schema
```js
{
    "weight": integer,  // [Mandatory] || Weight (relative to other weighted entries in the same list) of this object.
    "quality": float,   // [Optional]  || Quality of this object. Used when a luck level is present in the selection context.
    "mainhands": [      // [Optional]  || List of weighted stacks for the main hand.
        WeightedItemStack
    ],
    "offhands": [       // [Optional]  || List of weighted stacks for the off hand.
        WeightedItemStack
    ],
    "helmets": [        // [Optional]  || List of weighted stacks for the head slot.
        WeightedItemStack
    ],
    "chestplates": [    // [Optional]  || List of weighted stacks for the chest slot.
        WeightedItemStack
    ],
    "leggings": [       // [Optional]  || List of weighted stacks for the legs slot.
        WeightedItemStack
    ],
    "boots": [          // [Optional]  || List of weighted stacks for the boots slot.
        WeightedItemStack
    ],
    "tags": [           // [Optional]  || List of tags this gear set belongs to.
        "string"
    ]
}
```

GearSet tags are used by [SetPredicates](./SetPredicate.md) to specify sets of a specific tag.

# Examples
A simple iron gear set with a weight of 10.
```json
{
    "weight": 10,
    "quality": 0,
    "mainhands": [{
            "weight": 1,
            "stack": {
                "item": "minecraft:iron_sword"
            }
        }
    ],
    "offhands": [],
    "boots": [{
        "weight": 1,
        "stack": {
            "item": "minecraft:iron_boots"
        }
    }],
    "leggings": [{
        "weight": 1,
        "stack": {
            "item": "minecraft:iron_leggings"
        }
    }],
    "chestplates": [{
        "weight": 1,
        "stack": {
            "item": "minecraft:iron_chestplate"
        }
    }],
    "helmets": [{
        "weight": 1,
        "stack": {
            "item": "minecraft:iron_helmet"
        }
    }],
    "tags": [
    ]
}
```
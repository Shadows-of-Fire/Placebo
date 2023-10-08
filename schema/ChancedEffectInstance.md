# Description
A Chanced Effect Instance is a mob effect instance with an application chance and a random amplifier.

### Mob Effect Instances
A Mob Effect Instance is a combination of a [Mob Effect](https://minecraft.wiki/w/Effect) (an Effect, for short), a duration, and an amplifier.  
They are not to be confused with a [Potion](https://minecraft.wiki/w/Potion#Effect_potions), which is one or more Effect Instances that are applied to a potion-like item (Potions, Tipped Arrows, etc).  
When applied to an entity, an Effect Instance will tick down, applying the Effect until the duration runs out.  

# Dependencies
This object references the following objects:
1. [StepFunction](./StepFunction.md)

# Schema
```js
{
    "chance": float,          // [Optional]  || Chance that this potion will be applied. 1.0 = 100% chance. Default value = 1.0.
    "effect": "string",       // [Mandatory] || Registry name of the Effect to use.
    "amplifier": StepFunction // [Optional]  || Range of possible amplifiers. The output of the function will be truncated to an integer. Default value = 0.
    "ambient": boolean,       // [Optional]  || If this effect is marked as ambient. Default value = true.
    "visible": boolean        // [Optional]  || If this effect is marked as visible. Default value = false.
}
```

Consumers of this object may use it in constant mode, in which the `"chance"` field is omitted and the `"amplifier"` field only accepts an integer.

# Examples
A chanced effect instance providing Swiftness I - II, with an 80% chance to apply.
```json
{
    "chance": 0.8,
    "effect": "minecraft:speed",
    "amplifier": {
        "min": 0,
        "steps": 1,
        "step": 1
    }
}
```
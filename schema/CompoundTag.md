# Description
A [Compound Tag](https://minecraft.wiki/w/NBT_format) is how minecraft stores most binary data. Compound tags hold many data types, and can be nested.  
Compound Tags are part of Vanilla Minecraft and are not supplied by a mod.

# Schema
```js
Object // [Mandatory] || Object representation of a Compound Tag, which is just write a JSON object that maps to your desired nbt.

OR

"string" // [Mandatory] || String representation of a Compound Tag -- See https://github.com/aramperes/Mojangson or https://minecraft.wiki/w/NBT_format#Conversion_to_SNBT for more information.
```

Compound Tags do not have an explicit list of fields, as they are an unbounded map. What data is expected to be in a specific compound tag is implementation dependent.

# Examples
An example of object form is:
```json
{
    "item": "minecraft:netherite_sword",
    "count": 1,
    "nbt": {
        "Damage": 0
    }
}
```

The same data in string form is: `"{\"item\": \"minecraft:netherite_sword\", \"count\": 1b, \"nbt\": { \"Damage\": 0b }}"`. Note that as a JSON string, all inner quotes must be escaped.

# Misc
Placebo provides the following commands to help working with NBT data:
1. `/placebo string_to_obj` - Converts NBT in String form to Object form.
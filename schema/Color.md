# Description
A [Color](https://minecraft.wiki/w/Formatting_codes) is a color. Not much else to say there.

# Schema
```js
"string"  // [Mandatory] || A string representation of a Color. Must be either the name of a color, or a hex code in RGB format.
```

The names of all vanilla colors are listed [here](https://minecraft.wiki/w/Formatting_codes#Color_codes), in the `Name` column of the table.  
Mods may provide additional colors. For example, placebo provides the `"rainbow"` color.

In hex form takes the value `"#RRGGBB"`, meaning that all-red is `"#FF0000"`, all-green is `"#00FF00"`, and all-blue is `"#0000FF"`.

Note: for objects that use Color, `"color": Color` implies -> `"color": "#RRGGBB"`, since this class is a string primitive.
# Description
A Random Attribute Modifier represents an attribute modifier with a random value.  

### Attribute Modifiers
Each Attribute Modifier for a specific attribute provides an Operation and a value.  
The meaning of the value depends on the operation.  

There are three valid operations: Addition, Multiply Base, and Multiply Total. They are executed in order.

* Addition adds the given modifier to the base value of the attribute. This changes the effective base value for the next step.
* Multiply Base adds `modifier * effective base value` to the final value.
* Multiply Total multiplies the final value by `1.0 + modifier`.

The Attribute has the ability to clamp the final modified value, so the result of some modifiers may be ignored.

For example, given an attribute with a base value of 1, applying an Addition modifier with a value of 1 would result in a value of 2 (1 + 1).  
Additionally applying a Multiply Base modifier with a value of 1.5 would result in a value of (2 + 1.5 * 2).  
Further applying a Multiply Total modifier with a value of 0.75 would result in a value of 8.75 (5.0 * (1 + 0.75)).  

# Dependencies
This object references the following objects:
1. [StepFunction](./StepFunction.md)

# Schema
```js
{
    "attribute": "string",    // [Mandatory] || The registry name of the Attribute to modify.
    "operation": "string",    // [Mandatory] || The operation of the modifier. One of "addition", "multiply_base", or "multiply_total".
    "value": StepFunction     // [Mandatory] || The value of the attribute modifier.
}
```

Consumers of this object may use it in constant mode, in which the `"value"` key only accepts a float.

# Examples
A step function that may produce a value in the range 1..2 in intervals of 0.25. Meaning one of `[1, 1.25, 1.5, 1.75, 2]`.
```json
{
    "attribute": "minecraft:generic.max_health",
    "operation": "multiply_total",
    "value": {
        "min": 1,
        "steps": 4,
        "step": 0.25
    }
}
```
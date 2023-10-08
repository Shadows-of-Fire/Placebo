# Description
A Step Function is a utility for interpolating values while only producing "clean" outputs.  
Specifically, it maps a value in the range 0..1 to a value in the range X..Y, while only producing values that are evenly divisible by a number K.  

This is done using the following formula, where `level` is a value in 0..1:
```java
output = min + steps * (level + 0.5F / steps) * step;
```

The maximum value of a step function is equal to `min + steps * step`.

# Schema
```js
{
    "min": float,     // [Mandatory] || Minimum value of the output range.
    "steps": integer, // [Mandatory] || The total number of steps that the function has. Must be greater than or equal to one.
    "step": float     // [Mandatory] || The value that an individual step will increase the output by.
}

OR

float                 // [Mandatory] || Encodes a constant value as a StepFunction.
```

Some implementations may only accept one of the above. If unspecified, it can be assumed to be both.

# Examples
A step function that may produce a value in the range 1..2 in intervals of 0.25. Meaning one of `[1, 1.25, 1.5, 1.75, 2]`.
```json
{
    "min": 1,
    "steps": 4,
    "step": 0.25
}
```
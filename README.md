# Vague - Java Edition 
> This is the only standard compliant implementation of Vague.

## More defined spec
- \+ pop one element and add it to the accumulator
- \- decrement the accumulator
- ! print the accumulators value (unicode for ints and true/false for bools)
- \> move the bottom element to the top (depends on mode)
- < move the top element to the bottom (depends on mode)
- & pop one element and bitwise nand it with the accumulator
- = push the accumulators value / push a new stack
- _ pop a value and set the accumulator to it / remove the top stack
- . exit the program
- 0 set the accumulator to zero
- \* rainbow mode
- 2 set to 2d mode: push pop left and right act on the stack of values
- 1 set to 1d mode: push pop left and right act on the stack of stacks
- t push true
- f push false
- ( start bf style loop
- ) end bf style loop
package io.github.mattidragon.vague;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import java.util.*;

public class Program {
    private static final Random RANDOM = new Random();
    private final List<Instruction> instructions;
    
    public Program(String program) {
        instructions = program.chars()
                .mapToObj(Instruction::ofChar)
                .filter(Objects::nonNull)
                .toList();
        int counter = 0;
        for (Instruction instruction : instructions) {
            if (instruction == Instruction.START) {
                counter++;
            } else if (instruction == Instruction.END_START) {
                counter--;
            }
        }
        if (counter != 0) {
            throw new IllegalStateException("Unmatched parentheses");
        }
    }
    
    public void run() {
        // The magic of var: accessing fields of anonymous classes!
        var state = new State();
        
        loop:
        for (int i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            switch (instruction) {
                case ADD -> state.accumulator = state.pop().add(state.accumulator);
                case DECREMENT -> state.accumulator = state.accumulator.decrement();
                case PRINT -> {
                    var chars = state.accumulator.getValue().toCharArray();
                    var ansi = Ansi.ansi();
                    for (char c : chars) {
                        if (state.discouraged)
                            ansi.fgRgb(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256))
                                    .bgRgb(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256))
                                    .a(randomChoice(Attribute.INTENSITY_BOLD, Attribute.INTENSITY_BOLD_OFF, Attribute.INTENSITY_FAINT))
                                    .a(randomChoice(Attribute.UNDERLINE, Attribute.UNDERLINE_DOUBLE, Attribute.UNDERLINE_OFF))
                                    .a(randomChoice(Attribute.BLINK_OFF, Attribute.BLINK_FAST, Attribute.BLINK_SLOW))
                                    .a(randomChoice(Attribute.STRIKETHROUGH_OFF, Attribute.STRIKETHROUGH_ON))
                                    .a(randomChoice(Attribute.ITALIC, Attribute.ITALIC_OFF));
                        ansi.a(c);
                        if (state.discouraged)
                            ansi.reset();
                    }
                    System.out.print(ansi);
                }
                case RIGHT -> {
                    //Java freaks out without variable and external method...
                    List<?> stack = state.is2d ? state.data : state.data.get(0);
                    right(stack);
                }
                case LEFT -> {
                    //Java freaks out without variable and external method...
                    List<?> stack = state.is2d ? state.data : state.data.get(0);
                    left(stack);
                }
                case NAND -> state.accumulator = state.pop().nand(state.accumulator);
                case POP -> {
                    if (!state.is2d) {
                        state.popStack();
                    } else {
                        state.accumulator = state.pop();
                    }
                }
                case PUSH -> {
                    if (!state.is2d) {
                        state.pushStack();
                    } else {
                        state.push(state.accumulator);
                    }
                }
                case END -> {
                    break loop;
                }
                case ZERO -> state.accumulator = StackValue.ZERO;
                case DISCOURAGED -> state.discouraged = true;
                case D1 -> state.is2d = false;
                case D2 -> state.is2d = true;
                case TRUE -> state.push(StackValue.TRUE);
                case FALSE -> state.push(StackValue.FALSE);
                case START -> {
                    if (!StackValue.ZERO.equals(state.accumulator) && !StackValue.FALSE.equals(state.accumulator))
                        break;
                    // We have to jump
                    int pos = i + 1;
                    int stack = 0;
                    while (true) {
                        var instruction1 = instructions.get(pos);
                        if (instruction1 == Instruction.START) {
                            stack++;
                        } else if (instruction1 == Instruction.END_START) {
                            if (stack == 0) {
                                i = pos;
                                break;
                            } else {
                                stack--;
                            }
                        }
                        pos++;
                    }
                }
                case END_START -> {
                    if (StackValue.ZERO.equals(state.accumulator) || StackValue.FALSE.equals(state.accumulator))
                        break;
                    // We have to jump
                    int pos = i - 1;
                    int stack = 0;
                    while (true) {
                        var instruction1 = instructions.get(pos);
                        if (instruction1 == Instruction.END_START) {
                            stack++;
                        } else if (instruction1 == Instruction.START) {
                            if (stack == 0) {
                                i = pos;
                                break;
                            } else {
                                stack--;
                            }
                        }
                        pos--;
                    }
                }
            }
        }
    }
    
    @SafeVarargs
    private <T> T randomChoice(T... values) {
        return values[RANDOM.nextInt(values.length)];
    }
    
    private <T> void right(List<T> stack) {
        stack.add(0, stack.remove(stack.size() - 1));
    }
    
    private <T> void left(List<T> stack) {
        stack.add(stack.remove(0));
    }
    
    private enum Instruction {
        ADD('+'), // pop one element and add it to the accumulator
        DECREMENT('-'), // decrement the accumulator
        PRINT('!'), // print the accumulator (unicode for ints and true/false for bools)
        RIGHT('>'), // move the bottom element to the top (depends on mode)
        LEFT('<'), // move the top element to the bottom (depends on mode)
        NAND('&'), // pop one element and bitwise nand it with the accumulator
        PUSH('='), // push the accumulators value / push a new stack
        POP('_'), // pop a value and set the accumulator to it / remove the top stack
        END('.'), // exit the program
        ZERO('0'), // set the accumulator to zero
        DISCOURAGED('*'), // rainbow mode
        D2('2'), // set to 2d mode: push, pop, left and right act on the stack of values
        D1('1'), // set to 1d mode: push, pop, left and right act on the stack of stacks
        TRUE('t'), // push true
        FALSE('f'), // push false
        START('('), // start bf style loop
        END_START(')'); // end bf style loop
        
        private final char c;
        
        Instruction(char c) {
            this.c = c;
        }
        
        public static Instruction ofChar(int c) {
            for (Instruction i : Instruction.values())
                if (i.c == c) return i;
            return null;
        }
    }
    
    private static class State {
        public boolean is2d = false;
        public final List<List<StackValue>> data = new ArrayList<>(Collections.singleton(new ArrayList<>()));
        public StackValue accumulator = new StackValue.IntegerStackValue(0);
        public boolean discouraged = false;
        
        public void push(StackValue value) {
            if (data.size() == 0)
                throw new IllegalStateException("Empty top level stack!");
            data.get(0).add(value);
        }
        
        public StackValue pop() {
            if (data.size() == 0)
                throw new IllegalStateException("Empty top level stack!");
            if (data.get(0).size() == 0)
                throw new IllegalStateException("Empty value stack!");
            return data.get(0).remove(0);
        }
        
        public void pushStack() {
            data.add(new ArrayList<>());
        }
        
        public void popStack() {
            if (data.size() == 0)
                throw new IllegalStateException("Empty top level stack!");
            data.remove(0);
        }
    }
}

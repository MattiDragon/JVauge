package io.github.mattidragon.vague;

public sealed interface StackValue {
    IntegerStackValue ZERO = new IntegerStackValue(0);
    BooleanStackValue TRUE = new BooleanStackValue(true);
    BooleanStackValue FALSE = new BooleanStackValue(false);
    
    StackValue add(StackValue other);
    StackValue decrement();
    StackValue nand(StackValue other);
    String getValue();
    
    record IntegerStackValue(int value) implements StackValue {
        @Override
        public StackValue add(StackValue other) {
            if (other instanceof IntegerStackValue otherInt) {
                return new IntegerStackValue(value + otherInt.value);
            } else if (other instanceof BooleanStackValue otherBoolean) {
                return new IntegerStackValue(value + (otherBoolean.value ? 1 : 0));
            } else throw new IllegalStateException();
        }
    
        @Override
        public StackValue decrement() {
            return new IntegerStackValue(value - 1);
        }
    
        @Override
        public StackValue nand(StackValue other) {
            if (other instanceof IntegerStackValue otherInt) {
                return new IntegerStackValue(~(value & otherInt.value));
            } else if (other instanceof BooleanStackValue otherBoolean) {
                return new IntegerStackValue(~(value & (otherBoolean.value ? -1 : 0)));
            } else throw new IllegalStateException();
        }
    
        @Override
        public String getValue() {
            return String.valueOf((char) value);
        }
    }
    
    record BooleanStackValue(boolean value) implements StackValue {
        @Override
        public StackValue add(StackValue other) {
            if (other instanceof IntegerStackValue otherInt) {
                return new IntegerStackValue((value ? 1 : 0) + otherInt.value);
            } else if (other instanceof BooleanStackValue otherBoolean) {
                return new IntegerStackValue((value ? 1 : 0) + (otherBoolean.value ? 1 : 0));
            } else throw new IllegalStateException();
        }
    
        @Override
        public StackValue decrement() {
            return value ? new BooleanStackValue(false) : new IntegerStackValue(-1);
        }
    
        @Override
        public StackValue nand(StackValue other) {
            if (other instanceof IntegerStackValue otherInt) {
                return new IntegerStackValue(~((value ? -1 : 0) & otherInt.value));
            } else if (other instanceof BooleanStackValue otherBoolean) {
                return new BooleanStackValue(!(value && otherBoolean.value));
            } else throw new IllegalStateException();
        }
    
        @Override
        public String getValue() {
            return String.valueOf(value);
        }
    }
}

package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Store extends Instruction  {
    private LlvmIrValue leftValue;
    private LlvmIrValue rightValue;
    private Alloca alloca;
    public Store(String name,ValueType valueType,LlvmIrValue leftValue,LlvmIrValue rightValue) { // store leftValue rightValue
        super(name,valueType);
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public LlvmIrValue getLeftValue() {
        return leftValue;
    }

    public LlvmIrValue getRightValue() {
        return rightValue;
    }

    @Override
    public String midOutput() {
        String s = "";
        s = s + "store " + leftValue.getType().midOutput() + " " + leftValue.getName() + ", " +
                leftValue.getType().midOutput() + "* " + rightValue.getName() + "\n";
        return s;
    }
}

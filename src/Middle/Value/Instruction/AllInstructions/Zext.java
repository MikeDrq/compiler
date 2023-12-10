package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Zext extends Instruction {
    private LlvmIrValue value;
    public Zext(String name, ValueType valueType, LlvmIrValue llvmIrValue) {
        super(name,valueType);
        this.value = llvmIrValue;
    }

    public String getValueName() {
        return value.getName();
    }

    @Override
    public LlvmIrValue getDefine() {
        return this;
    }

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        h.add(value);
        return h;
    }

    @Override
    public void change(String name,LlvmIrValue llvmIrValue) {
        if (this.value.getName().equals(name)) {
            this.value = llvmIrValue;
        } else {
            System.out.println("error when replace load");
        }
    }

    @Override
    public String midOutput() {
        return super.getName() + " = zext i1 " + value.getName() + " to i32\n";
    }
}

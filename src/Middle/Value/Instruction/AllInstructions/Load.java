package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Load extends Instruction {
    LlvmIrValue llvmIrValue;
    public Load(String name, ValueType valueType,LlvmIrValue llvmIrValue) {
        super(name,valueType);
        this.llvmIrValue = llvmIrValue;
    }

    public LlvmIrValue getLlvmIrValue() {
        return llvmIrValue;
    }

    @Override
    public void setDim(int dim) {
        super.setDim(dim);
    }

    @Override
    public void setRParamDim(int dim) {
        super.setRParamDim(dim);
    }

    @Override
    public String midOutput() {
        String s = "";
        s = s + super.getName() + " = load " + super.getType().midOutput() + ", "
                + super.getType().midOutput() + "* " + llvmIrValue.getName() + "\n";
        return s;
    }

    @Override
    public LlvmIrValue getDefine() {
        return this;
    }

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        h.add(llvmIrValue);
        return h;
    }

    @Override
    public void change(String name,LlvmIrValue l) {
        if (l.getName().equals(name)) {
            this.llvmIrValue = l;
        } else {
            System.out.println("error when replace load");
        }
    }
}

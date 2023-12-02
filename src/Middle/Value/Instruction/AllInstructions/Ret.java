package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Ret extends Instruction {
    private boolean isVoid;
    private LlvmIrValue right;
    public Ret(String name, ValueType valueType, LlvmIrValue right) {
        super(name,valueType);
        if (right == null) {
            isVoid = true;
        } else {
            isVoid = false;
        }
        this.right = right;
    }

    public Boolean getIsVoid() {
        return this.isVoid;
    }

    public LlvmIrValue getRight() {
        return this.right;
    }

    @Override
    public String midOutput() {
        String s = "";
        s = s + "ret ";
        if (isVoid) {
            s = s + "void\n";
        } else {
            s = s + right.getType().midOutput() + " " + right.getName() + "\n";
        }
        return s;
    }
}

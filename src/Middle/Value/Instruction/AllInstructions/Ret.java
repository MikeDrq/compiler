package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

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

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        if (!isVoid) {
            h.add(right);
        }
        return h;
    }


    @Override
    public void change(String name,LlvmIrValue llvmIrValue) {
       if (!isVoid) {
           if (right.getName().equals(name)) {
               right = llvmIrValue;
           } else {
               System.out.println("error when replace load");
           }
       }
    }
}

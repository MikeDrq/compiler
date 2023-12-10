package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Type.PointerType;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Alloca extends Instruction {
    private LlvmIrValue llvmIrValue;
    public Alloca(ValueType valueType,String name,LlvmIrValue llvmIrValue) {
        super(name,valueType);
        super.setDim(llvmIrValue.getDim());
        this.llvmIrValue = llvmIrValue;
    }

    public LlvmIrValue getLlvmIrValue() {
        return llvmIrValue;
    }
    //<result> = alloca <type>
    @Override
    public String midOutput() {
        String s = "";
        /*if(llvmIrValue.getDim() == 0) {
            s = s + super.getName() + " = alloca i32" + "\n";
        } else if (llvmIrValue.getDim() == 1) {
            s = s + super.getName() + " = alloca [" + llvmIrValue.getColumn() + " x i32]\n";
        } else if (llvmIrValue.getDim() == 2) {
            s = s + super.getName() + " = alloca [" + llvmIrValue.getRaw() + " x [" + llvmIrValue.getColumn() + " x i32]]\n";
        } else {
            System.out.println("error");
        }*/
        s = s + super.getName() + " = alloca " + super.getType().midOutput() + "\n";
        return s;
    }

    @Override
    public LlvmIrValue getDefine() {
        return this;
    }
}

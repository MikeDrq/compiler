package Middle.Value.Instruction;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.BasicBlock.BasicBlock;

import java.util.ArrayList;

public class Instruction extends LlvmIrValue {
    private BasicBlock basicBlock;

    public Instruction(String name,ValueType valueType) { //指令
        super(name,valueType);
    }

    public Instruction(String name, ValueType valueType,BasicBlock basicBlock) {
        super(name,valueType);
        this.basicBlock = basicBlock;
    }

    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        return h;
    }

    public void changeName(String name) {
        super.changeName(name);
    }

    public void change(String name,LlvmIrValue llvmIrValue) {

    }

    public String midOutput() {
        String s = "";
        return s;
    }


}

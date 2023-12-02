package Middle.Value.Instruction;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.BasicBlock.BasicBlock;

public class Instruction extends LlvmIrValue {
    private BasicBlock basicBlock;

    public Instruction(String name,ValueType valueType) { //指令
        super(name,valueType);
    }

    public Instruction(String name, ValueType valueType,BasicBlock basicBlock) {
        super(name,valueType);
        this.basicBlock = basicBlock;
    }

    public String midOutput() {
        String s = "";
        return s;
    }

}

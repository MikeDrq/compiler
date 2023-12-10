package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class MidMove extends Instruction {

    private LlvmIrValue src;
    private LlvmIrValue dst;
    public MidMove(String name, ValueType valueType, LlvmIrValue src, LlvmIrValue dst) {
        super(name,valueType);
        this.src = src;
        this.dst = dst;
    }

    public void setSrc(LlvmIrValue src) {
        this.src = src;
    }

    public LlvmIrValue getDst() {
        return this.dst;
    }

    public LlvmIrValue getSrc() {
        return src;
    }

    @Override
    public LlvmIrValue getDefine(){
        return this.dst;
    }

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> op = new ArrayList<>();
        op.add(src);
        return op;
    }

    @Override
    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("move ").append(dst.getName()).append(", ").append(src.getName()).append("\n");
        return sb.toString();
    }
}

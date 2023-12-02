package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Br extends Instruction {
    private LlvmIrValue trueLabel;
    private LlvmIrValue falseLabel;
    private LlvmIrValue jump;
    private Boolean hasTrueLabel;
    private Boolean hasFalseLabel;
    private Boolean jumpWithNoCondition;
    private LlvmIrValue cond;
    public Br(String name, ValueType valueType,LlvmIrValue cond) {
        super(name,valueType);
        this.cond = cond;
        this.hasTrueLabel = true;
        this.hasFalseLabel = true;
        this.jumpWithNoCondition = false;
    }

    public Br(String name,ValueType valueType) {
        super(name,valueType);
        this.hasTrueLabel = false;
        this.hasFalseLabel = false;
        this.jumpWithNoCondition = true;
    }

    public void setTrueLabel(LlvmIrValue llvmIrValue) {
        this.trueLabel = llvmIrValue;
    }

    public void setFalseLabel(LlvmIrValue llvmIrValue) {
        this.falseLabel = llvmIrValue;
    }

    public LlvmIrValue getTrueLabel() {
        return trueLabel;
    }

    public LlvmIrValue getFalseLabel() {
        return falseLabel;
    }

    public Boolean getHasTrueLabel() {
        return this.hasTrueLabel;
    }

    public void setJump(LlvmIrValue jump) {
        this.jump = jump;
    }

    public Boolean getHasFalseLabel() {
        return hasFalseLabel;
    }

    public Boolean getJumpWithNoCondition() {
        return jumpWithNoCondition;
    }

    public String getJumpName() {
        return jump.getName();
    }

    public String getCondName() {
        return cond.getName();
    }

    @Override
    public String midOutput() {
        String s = "";
        if (!jumpWithNoCondition) {
            s = "br i1 " + cond.getName() + ",label %" + trueLabel.getName() + ",label %" + falseLabel.getName() + "\n";
        } else {
            s = "br label %" + jump.getName() + "\n";
        }
        return s;
    }

}

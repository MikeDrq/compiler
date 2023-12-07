package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;
import Middle.Value.Instruction.InstructionType;
import Mips.MipsInstruction.Li;

import java.util.ArrayList;

public class Calculate extends Instruction {
    private InstructionType instructionType;
    private LlvmIrValue left;
    private LlvmIrValue right;
    private String cond = null;

    public Calculate(String name, LlvmIrValue leftValue, LlvmIrValue rightValue,
                      InstructionType instructionType, ValueType valueType) {
        super(name,valueType);
        this.instructionType = instructionType;
        this.left = leftValue;
        this.right = rightValue;
    }

    public Calculate(String name, LlvmIrValue leftValue, LlvmIrValue rightValue,
                     InstructionType instructionType, ValueType valueType,String cond) {
        super(name,valueType);
        this.instructionType = instructionType;
        this.cond = cond;
        this.left = leftValue;
        this.right = rightValue;
    }

    public InstructionType getInstructionType() {
        return this.instructionType;
    }

    public LlvmIrValue getLeft() {
        return left;
    }

    public LlvmIrValue getRight() {
        return right;
    }

    public String getCond() {
        return cond;
    }

    @Override
    public String midOutput() {
        String s = "";
        String op = "";
        if (instructionType == InstructionType.add) {
            op = "add";
        } else if (instructionType == InstructionType.sub) {
            op = "sub";
        } else if (instructionType == InstructionType.mul) {
            op = "mul";
        } else if (instructionType == InstructionType.sdiv) {
            op = "sdiv";
        } else if (instructionType == InstructionType.srem) {
            op = "srem";
        } else if (instructionType == InstructionType.xor) {
           op = "xor";
        } else if (instructionType == InstructionType.icmp) {
            op = "icmp";
        }
        if (op.equals("icmp")) {
            s = s + super.getName() + " = " + op + " " + cond + " i32 ";
            s = s + left.getName() + ", " + right.getName() + "\n";
        } else {
            s = s + super.getName() + " = " + op + " ";
            s = s + " i32 " + left.getName() + ", " + right.getName() + "\n";
        }
        return s;
    }

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        h.add(left);
        h.add(right);
        return h;
    }

    @Override
    public void change(String name,LlvmIrValue llvmIrValue) {
        if (left.getName().equals(name)) {
            this.left = llvmIrValue;
        }
        if (right.getName().equals(name)) {
            this.right = llvmIrValue;
        }
        System.out.println("-------------------------");
        System.out.println(this.midOutput());
    }
}

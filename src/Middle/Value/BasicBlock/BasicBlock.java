package Middle.Value.BasicBlock;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.AllInstructions.Br;
import Middle.Value.Instruction.AllInstructions.Label;
import Middle.Value.Instruction.Instruction;
import SyntaxTree.DeclNode;
import SyntaxTree.StmtNode;

import java.util.ArrayList;

public class BasicBlock extends LlvmIrValue {
    private ArrayList<Instruction> instructions;
    private ArrayList<Br> continues = new ArrayList<>();
    private ArrayList<Br> breaks = new ArrayList<>();

    public BasicBlock(String name, ValueType valueType) {
        super(name,valueType);
        instructions = new ArrayList<>();
    }

    public void addInstructions(ArrayList<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            this.instructions.add(instruction);
        }
    }

    public void addOneInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void addAContinue(Br br) {
        continues.add(br);
    }

    public void addABreak(Br br) {
        breaks.add(br);
    }

    public void setBC(LlvmIrValue forBreak, LlvmIrValue forContinue) {
        if (breaks.size() != 0) {
            for (Br b : breaks) {
                b.setJump(forBreak);
            }
        }
        if (continues.size() != 0) {
            for (Br b : continues) {
                b.setJump(forContinue);
            }
        }
        breaks = new ArrayList<>();
        continues = new ArrayList<>();
    }

    public String midOutput() {
        String s = "";
        if (!super.getName().equals("-1")) {
            for (Instruction instruction : instructions) {
                s = s + instruction.midOutput();
            }
        }
        System.out.println("block name:" + super.getName());
        return s;
    }
}

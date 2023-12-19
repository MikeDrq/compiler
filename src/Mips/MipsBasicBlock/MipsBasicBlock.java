package Mips.MipsBasicBlock;

import Mips.MipsInstruction.J;
import Mips.MipsInstruction.MipsInstruction;
import Mips.MipsValue;

import java.util.ArrayList;

public class MipsBasicBlock implements MipsValue {

    private ArrayList<MipsInstruction> mipsInstructions;

    public MipsBasicBlock () {
        this.mipsInstructions = new ArrayList<>();
    }

    public void addInstruction(MipsInstruction mipsInstruction) {
        this.mipsInstructions.add(mipsInstruction);
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        for (MipsInstruction mipsInstruction : mipsInstructions) {
            sb.append(mipsInstruction.mipsOutput());
        }
        return sb.toString();
    }
}

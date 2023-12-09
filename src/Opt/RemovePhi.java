package Opt;

import Middle.LlvmIrModule;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.BasicBlock.BasicBlockCnt;
import Middle.Value.Func.Func;
import Middle.Value.Instruction.AllInstructions.Alloca;
import Middle.Value.Instruction.AllInstructions.Br;
import Middle.Value.Instruction.AllInstructions.Phi;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class RemovePhi {
    private LlvmIrModule llvmIrModule;
    private BasicBlockCnt basicBlockCnt;

    public RemovePhi(LlvmIrModule llvmIrModule,BasicBlockCnt basicBlockCnt) {
        this.llvmIrModule = llvmIrModule;
        this.basicBlockCnt = basicBlockCnt;
    }

    public void doRemovePhi() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func : funcs) {
            insertPC(func);
        }
    }

    private Boolean containPhi(BasicBlock basicBlock) {
        LinkedList<Instruction> instructions = basicBlock.getInstructions();
        for (Instruction instruction : instructions) {
            if (instruction instanceof Phi) {
                return true;
            }
        }
        return false;
    }

    private void insertPC(Func func) {
        HashMap<String,ArrayList<String>> next = func.getNext();
        HashMap<String,ArrayList<String>> prev = func.getPrev();
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            if (!containPhi(basicBlock)) {
                continue;
            }
            ArrayList<String> blockPrev = next.get(basicBlock.getName());

        }
    }
}

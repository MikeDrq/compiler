package Mips.MipsFunc;

import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Mips.MipsBasicBlock.MipsBasicBlock;
import Mips.MipsBasicBlock.MipsBasicBlockBuilder;
import Mips.MipsSymbolTable.MipsSymbol;
import Mips.MipsSymbolTable.MipsSymbolTable;
import Mips.MipsValue;

import java.util.ArrayList;

public class MipsFunc implements MipsValue {
    private String name;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;


    public MipsFunc(String name) {
        this.name = name;
        this.mipsBasicBlocks = new ArrayList<>();
    }

    public void addMipsBasicBlock(MipsBasicBlock mipsBasicBlock) {
        mipsBasicBlocks.add(mipsBasicBlock);
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + name + ":\n");
        for (MipsBasicBlock mipsBasicBlock : mipsBasicBlocks) {
            sb.append(mipsBasicBlock.mipsOutput());
        }
        return sb.toString();
    }
}

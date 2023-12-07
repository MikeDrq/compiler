package Mem2Reg;

import Middle.LlvmIrModule;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;

import java.util.ArrayList;

public class MemToReg {
    public LlvmIrModule llvmIrModule;

    public MemToReg(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
    }

    public void doMemToReg() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func:funcs) {
            func.doMemToReg();
        }
    }
}

package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Value.BasicBlock.BasicBlockCnt;

public class Opt {
    private LlvmIrModule llvmIrModule;
    private BasicBlockCnt basicBlockCnt;

    public Opt(LlvmIrModule llvmIrModule,BasicBlockCnt basicBlockCnt) {
        this.llvmIrModule = llvmIrModule;
        this.basicBlockCnt = basicBlockCnt;
    }

    public void doOpt() {
        MemToReg memToReg = new MemToReg(llvmIrModule);
        memToReg.doMemToReg(); //mem2reg优化
        RemovePhi removePhi = new RemovePhi(llvmIrModule,basicBlockCnt);
        removePhi.doRemovePhi();
        RegAllocate regAllocate = new RegAllocate(llvmIrModule);
        regAllocate.doRegAllocate();
    }
}

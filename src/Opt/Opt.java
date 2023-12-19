package Opt;

import Middle.LlvmIrModule;

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
        FuncInlining funcInlining = new FuncInlining(llvmIrModule);
        funcInlining.doFuncInlining();
        Gvn gvn = new Gvn(llvmIrModule);
        gvn.doGvn();
        RegAllocate regAllocate = new RegAllocate(llvmIrModule);
        regAllocate.doRegAllocate();
        RemovePhi removePhi = new RemovePhi(llvmIrModule,basicBlockCnt);
        removePhi.doRemovePhi();
    }
}

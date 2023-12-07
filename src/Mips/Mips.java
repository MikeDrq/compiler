package Mips;

import Middle.LlvmIrModule;
import Middle.Value.Func.Func;
import Middle.Value.GlobalVar.GlobalVar;
import Middle.Value.GlobalVar.GlobalVarBuilder;
import Mips.MipsFunc.MipsFunc;
import Mips.MipsFunc.MipsFuncBuilder;
import Mips.MipsFunc.StringCnt;
import Mips.MipsGlobalVar.MipsGlobalVar;
import Mips.MipsGlobalVar.MipsGlobalVarBuilder;
import Mips.MipsSymbolTable.MipsSymbol;
import Mips.MipsSymbolTable.MipsSymbolTable;
import SymbolTable.Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class Mips {
    private LlvmIrModule llvmIrModule;

    public Mips(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
    }

    public MipsModule generateMips() {
        MipsModule mipsModule = new MipsModule();
        ArrayList<GlobalVar> globalVars = llvmIrModule.getGlobalVars();
        MipsGlobalVarBuilder mipsGlobalVarBuilder = new MipsGlobalVarBuilder(globalVars);
        ArrayList<MipsGlobalVar> mipsGlobalVars = mipsGlobalVarBuilder.generateMipsGlobalVars();
        for (MipsGlobalVar mipsGlobalVar : mipsGlobalVars) {
            mipsModule.addMipsGlobalVar(mipsGlobalVar);
        }
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        StringCnt stringCnt = new StringCnt(); //字符串计数
        ArrayList<String> strings = new ArrayList<>(); //记录出现的字符串
        GlobalLabelCnt gbc = new GlobalLabelCnt();
        for (Func func : funcs) {
            MipsFuncBuilder mipsFuncBuilder = new MipsFuncBuilder(func,stringCnt,gbc);
            MipsFunc mipsFunc = mipsFuncBuilder.generateMipsFuncs(strings);
            mipsModule.addMipsFunc(mipsFunc);
            System.out.println("domain output: " + func.domainOutput());
        }
        mipsModule.setGlobalString(strings);
        return mipsModule;
    }
}

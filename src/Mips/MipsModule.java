package Mips;

import Middle.Value.GlobalVar.GlobalVar;
import Mips.MipsFunc.MipsFunc;
import Mips.MipsGlobalVar.MipsGlobalVar;

import java.util.ArrayList;

public class MipsModule{
    private ArrayList<MipsGlobalVar> mipsGlobalVars;
    private ArrayList<MipsFunc> mipsFuncs;
    private ArrayList<String> globalString;

    public MipsModule() {
        this.mipsGlobalVars = new ArrayList<>();
        this.mipsFuncs = new ArrayList<>();
        this.globalString = new ArrayList<>();
    }

    public void addMipsGlobalVar(MipsGlobalVar mipsGlobalVar) {
        mipsGlobalVars.add(mipsGlobalVar);
    }

    public void addMipsFunc(MipsFunc mipsFunc) {
        mipsFuncs.add(mipsFunc);
    }

    public void setGlobalString(ArrayList<String> globalString) {
        this.globalString = globalString;
    }

    public ArrayList<String> mipsOutput() {
        ArrayList<String> mipsGlobal = new ArrayList<>();
        mipsGlobal.add(".data:\n");
        for(MipsGlobalVar mipsGlobalVar : mipsGlobalVars) {
            mipsGlobal.add(mipsGlobalVar.mipsOutput());
        }
        ArrayList<String> mipsFunc = new ArrayList<>();
        mipsFunc.add("\n.text:\n");
        mipsFunc.add("li $fp, 0x10040000\n");
        mipsFunc.add("j main\n");
        for (MipsFunc mipsFunction : mipsFuncs) {
            mipsFunc.add(mipsFunction.mipsOutput());
        }
        ArrayList<String> mips = new ArrayList<>();
        for (String s : mipsGlobal) {
            mips.add(s);
        }
        for (String s : globalString) {
            mips.add(s);
        }
        for (String s : mipsFunc) {
            mips.add(s);
        }
        return mips;
    }
}

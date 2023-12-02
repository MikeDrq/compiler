package Mips.MipsGlobalVar;

import Middle.Value.GlobalVar.GlobalVar;
import Middle.Value.Instruction.AllInstructions.Alloca;

import java.util.ArrayList;

public class MipsGlobalVarBuilder {
    private ArrayList<GlobalVar> globalVars;

    public MipsGlobalVarBuilder(ArrayList<GlobalVar> globalVars) {
        this.globalVars = globalVars;
    }

    public ArrayList<MipsGlobalVar> generateMipsGlobalVars() {
        ArrayList<MipsGlobalVar> mipsGlobalVars = new ArrayList<>();
        for (GlobalVar globalVar : globalVars) {
            int dim = globalVar.getDim();
            if (dim == 0) {
                String name = globalVar.getName();
                int value = globalVar.getValue();
                MipsGlobalVar mipsGlobalVar = new MipsGlobalVar(dim,name,value);
                mipsGlobalVars.add(mipsGlobalVar);
            } else if (dim == 1) {
                String name = globalVar.getName();
                ArrayList<Integer> content = globalVar.getOneDimArray();
                MipsGlobalVar mipsGlobalVar = new MipsGlobalVar(dim,name,content);
                mipsGlobalVars.add(mipsGlobalVar);
            } else if (dim == 2) {
                String name = globalVar.getName();
                ArrayList<ArrayList<Integer>> content = globalVar.getTwoDimArray();
                MipsGlobalVar mipsGlobalVar = new MipsGlobalVar(dim,name,content,0);
                mipsGlobalVars.add(mipsGlobalVar);
            }
        }
        return mipsGlobalVars;
    }
}

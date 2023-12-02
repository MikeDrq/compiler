package Middle;

import Middle.Value.Func.Func;
import Middle.Value.GlobalVar.GlobalVar;

import java.util.ArrayList;

public class LlvmIrModule {
    private ArrayList<GlobalVar> globalVars;
    private ArrayList<Func> functions;

    public LlvmIrModule() {
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addGlobalVar(GlobalVar globalVar) {
        this.globalVars.add(globalVar);
    }

    public void addFunc(Func function) {
        this.functions.add(function);
    }

    public ArrayList<GlobalVar> getGlobalVars() {
        return this.globalVars;
    }

    public ArrayList<Func> getFunctions() {
        return this.functions;
    }

    public ArrayList<String> midOutput() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add("declare i32 @getint()\ndeclare void @putint(i32)\ndeclare void @putch(i32)\ndeclare void @putstr(i8*)\n");
        for (GlobalVar globalVar:globalVars) {
            ans.add(globalVar.midOutput());
        }
        for (Func func : functions) {
            ans.add(func.midOutput());
        }
        return ans;
    }
}

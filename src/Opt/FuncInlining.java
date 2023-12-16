package Opt;

import Middle.LlvmIrModule;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Instruction.AllInstructions.Call;
import Middle.Value.Instruction.Instruction;

import java.util.*;

public class FuncInlining {
    LlvmIrModule llvmIrModule;
    HashMap<String, HashSet<String>> map;
    HashMap<String,Boolean> isArrive;
    public FuncInlining(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
        this.map = new HashMap<>();
        this.isArrive = new HashMap<>();
    }

    public void walk(String cur) {
        HashSet<String> child = map.get(cur);
        if (!isArrive.get(cur)) {
            isArrive.put(cur,true);
            for (String s : child) {
                walk(s);
            }
        } else {
            return;
        }
    }

    public void doFuncInlining() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func : funcs) {
            isArrive.put(func.getName(),false);
            findCall(func);
        }
        if (!map.containsKey("@main")) {
            System.out.println("error,cannot find main");
        } else {
            //isArrive.put("@main",true);
            walk("@main");
        }

        Iterator<Func> iterator = funcs.iterator();
        while(iterator.hasNext()) {
            Func func = iterator.next();
            if (!isArrive.get(func.getName())) {
                iterator.remove();
            }
        }
    }

    public void findCall(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        HashSet<String> child = new HashSet<>();
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            for (Instruction instruction : instructions) {
                if (instruction instanceof Call) {
                    if (((Call) instruction).getMark() == 0) {
                        child.add(((Call) instruction).getFuncValue().getName());
                    }
                }
            }
        }
        map.put(func.getName(),child);
    }
}

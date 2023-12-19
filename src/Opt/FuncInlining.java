package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Instruction.AllInstructions.Call;
import Middle.Value.Instruction.AllInstructions.Getelementptr;
import Middle.Value.Instruction.AllInstructions.Store;
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

    public HashMap<String, Func> simplifyFuncs(ArrayList<Func> funcs) {
        HashMap<String,Func> records = new HashMap<>();
        for (Func func : funcs) {
            if (!func.getName().equals("@main")) {
                if (func.hasRetValue()) {
                    records.put(func.getName(),func);
                    continue;
                }
                ArrayList<LlvmIrValue> params = func.getParams();
                int flag = 0;
                ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
                for (BasicBlock basicBlock : basicBlocks) {
                    flag = 0;
                    LinkedList<Instruction> instructions = basicBlock.getInstructions();
                    for (Instruction instruction : instructions) {
                        if (instruction instanceof Call ) {
                            if (((Call) instruction).getFuncValue() == null) {
                                flag = 1;
                                break;
                            }
                            if (records.containsKey(((Call) instruction).getFuncValue().getName())) {
                                flag = 1;
                                break;
                            }
                        }
                        if (instruction instanceof Store) {
                            if (((Store) instruction).getRightValue().getName().charAt(0) == '@') {
                                flag = 1;
                                break;
                            }
                            if ((((Store) instruction).getRightValue()) instanceof Getelementptr) {
                                flag = 1;
                                break;
                            }
                        }
                    }
                    if (flag == 1) {
                        records.put(func.getName(),func);
                        break;
                    }
                }
            } else {
                records.put("@main",func);
            }
        }
        return records;
    }


    public void doFuncInlining() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();

        HashMap<String,Func> records = simplifyFuncs(funcs); //失败的删函数

        Iterator<Func> funcIterator = funcs.iterator();
        while(funcIterator.hasNext()) {
            if (!records.containsKey(funcIterator.next().getName())) {
                funcIterator.remove();
            }
        }

        for (Func func : funcs) {
            isArrive.put(func.getName(),false);
            findCall(func,records);
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

    public void findCall(Func func,HashMap<String,Func> records) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        HashSet<String> child = new HashSet<>();
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            Iterator<Instruction> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                Instruction instruction = iterator.next();
                if (instruction instanceof Call) {
                    if (((Call) instruction).getMark() == 0) {
                        if (records.containsKey(((Call) instruction).getFuncValue().getName()))
                            child.add(((Call) instruction).getFuncValue().getName());
                        else {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        map.put(func.getName(),child);
    }
}

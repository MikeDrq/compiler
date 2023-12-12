package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Instruction.AllInstructions.Phi;
import Middle.Value.Instruction.Instruction;
import Mips.Register;

import java.util.*;

public class RegAllocate {

    private LlvmIrModule llvmIrModule;
    private HashMap<String, HashSet<LlvmIrValue>> in = new HashMap<>();
    private HashMap<String, HashSet<LlvmIrValue>> out = new HashMap<>();
    private HashMap<Integer,LlvmIrValue> regVar; //寄存器存了什么变量
    private HashMap<LlvmIrValue,Integer> varReg; //变量存在那个寄存器中
    private ArrayList<Integer> regs; //所有可以用的寄存器

    public RegAllocate(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
        this.regs = new ArrayList<>();
        for (int i = 8;i <= 25;i++) {
            regs.add(i);
        }
    }

    public void doRegAllocate() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for(Func func : funcs) {
            buildUseDefine(func);
            initInOut(func);
            buildInOut(func);
            /*变量分析结束*/
            this.regVar = new HashMap<>();
            this.varReg = new HashMap<>();
            func.addVarReg(varReg);
            ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
            BasicBlock first = basicBlocks.get(0);
            allocateReg(first);
            Register r = new Register();
            System.out.println(func.getName());
            for (LlvmIrValue ins : varReg.keySet()) {
                if (ins.getName().equals("")) {
                    System.out.println(((Instruction) ins).midOutput());
                }
                System.out.println(ins.getName() + " -> " + r.getRegister(varReg.get(ins)));
            }
        }
     }

     public void initInOut(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            HashSet<LlvmIrValue> a = new HashSet<>();
            HashSet<LlvmIrValue> b = new HashSet<>();
            in.put(basicBlock.getName(),a);
            out.put(basicBlock.getName(),b);
        }
     }

     public void buildUseDefine(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            for (Instruction instruction : instructions) {
                ArrayList<LlvmIrValue> uses = instruction.getOperand();
                for (LlvmIrValue use : uses) {
                    basicBlock.setUse(use);
                }
                if (instruction.getDefine() != null) {
                    LlvmIrValue define = instruction.getDefine();
                    basicBlock.setDef(define);
                }
            }
        }
     }

     public BasicBlock findBasicBlock(String s,Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock b : basicBlocks) {
            if (b.getName().equals(s)) {
                return b;
            }
        }
        return null;
     }

     public void buildInOut(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        int flag;
        do {
            flag  = 0;
            for (int i = basicBlocks.size() - 1;i >= 0;i--) {
                BasicBlock cur = basicBlocks.get(i);
                ArrayList<String> next = cur.getNext();
                HashSet<LlvmIrValue> o = new HashSet<>();
                for (String s : next) {
                    if (!s.equals(cur.getName())) {
                        BasicBlock bb = findBasicBlock(s, func);
                        o.addAll(in.get(bb.getName()));
                    }
                }
                out.put(cur.getName(),o);
                HashSet<LlvmIrValue> ori = in.get(cur.getName());
                HashSet<LlvmIrValue> ne = new HashSet<>();
                ne.addAll(o);
                ne.removeAll(cur.getDef());
                ne.addAll(cur.getUse());
                if (!ne.equals(ori)) {
                    in.put(cur.getName(),ne);
                    flag = 1;
                }
            }

        } while(flag == 1);

        for(BasicBlock basicBlock : basicBlocks) {
            basicBlock.setIn(in.get(basicBlock.getName()));
            basicBlock.setOut(out.get(basicBlock.getName()));
        }

        for (BasicBlock basicBlock : basicBlocks) {
            System.out.println(basicBlock.getName());
            System.out.println("define:");
            for (LlvmIrValue llvmIrValue : basicBlock.getDef()) {
                System.out.print(llvmIrValue.getName() + " ");
            }
            System.out.println("\n");
            System.out.println("use:");
            for (LlvmIrValue llvmIrValue : basicBlock.getUse()) {
                System.out.print(llvmIrValue.getName() + " ");
            }
            System.out.println("\n");
            System.out.println("in:");
            for (LlvmIrValue llvmIrValue : basicBlock.getIn()) {
                System.out.print(llvmIrValue.getName() + " ");
            }
            System.out.println("\n");
            System.out.println("out:");
            for (LlvmIrValue llvmIrValue : basicBlock.getOut()) {
                System.out.print(llvmIrValue.getName()+" ");
            }
            System.out.println("\n");
        }
     }

     public void allocateReg(BasicBlock cur) {
        for (LlvmIrValue llvmIrValue : varReg.keySet()) {
            if (llvmIrValue.getName().equals("%v_61")) {
                System.out.println(cur.getName() + " " + llvmIrValue.getName() + " " + varReg.get(llvmIrValue));
            }
        }
        if (cur.getName().equals("3")) {
            System.out.println("great!");
        }
        HashMap<LlvmIrValue,Integer> lastPos = new HashMap<>();
        LinkedList<Instruction> instructions = cur.getInstructions();
        ArrayList<LlvmIrValue> curDef = new ArrayList<>();
        ArrayList<LlvmIrValue> tempRelease = new ArrayList<>();
        for (int i = 0;i < instructions.size();i++) {
            ArrayList<LlvmIrValue> operands = instructions.get(i).getOperand();
            for (LlvmIrValue llvmIrValue : operands) {
                if (llvmIrValue.getName().charAt(0) == '%' || llvmIrValue.getName().charAt(0) == '@' ) { //常数的话用$k0$k1直接解决
                    lastPos.put(llvmIrValue, i);
                }
            }
        }

        for (int i = 0;i < instructions.size();i++) {
            Instruction curInstr = instructions.get(i);
            ArrayList<LlvmIrValue> operands = curInstr.getOperand();
            if (!(curInstr instanceof Phi)) {
                for (LlvmIrValue operand : operands) {
                    if (lastPos.containsKey(operand) && lastPos.get(operand) == i && !out.get(cur.getName()).contains(operand) && varReg.containsKey(operand)) {
                        //本块内不会使用,且不在out集中，可以暂时释放
                        regVar.remove(varReg.get(operand));
                        tempRelease.add(operand);
                    }
                }
            }

            if (curInstr.getDefine() != null) {//有定义,需要寄存器
                int num = findAReg();
                curDef.add(curInstr);
                if (!regVar.containsKey(num)) {
                    regVar.put(num,curInstr);
                    varReg.put(curInstr,num);
                } else {
                    varReg.remove(regVar.get(num));
                }
            }
        }

        ArrayList<BasicBlock>  domain = cur.getDomain();
        for (BasicBlock b : domain) {
            HashMap<Integer,LlvmIrValue> store = new HashMap<>();
            //记录哪些寄存器被释放
            HashSet<LlvmIrValue> in = b.getIn();
            for (Integer num : regVar.keySet()) {
                if (!in.contains(regVar.get(num))) {
                    store.put(num,regVar.get(num));
                }
            }
            //释放
            for (Integer num : store.keySet()) {
                regVar.remove(num);
            }

            allocateReg(b);
            //恢复被释放的寄存器
            for (Integer num : store.keySet()) {
                regVar.put(num,store.get(num));
            }
        }

        //删除本快内寄存器映射
         for (LlvmIrValue llvmIrValue : curDef) {
             if (varReg.containsKey(llvmIrValue)) {
                 regVar.remove(varReg.get(llvmIrValue));
             }
         }

        //恢复cur不用但是定义在之前块的reg
        for (LlvmIrValue llvmIrValue : tempRelease) {
            if (varReg.containsKey(llvmIrValue) && !curDef.contains(llvmIrValue)) {
                regVar.put(varReg.get(llvmIrValue),llvmIrValue);
            }
        }
     }

     public int findAReg() {
        for (Integer num : regs) {
            if (!regVar.containsKey(num)) {
                return num;
            }
        }
        return regs.get(0);//寄存器已满，释放$t0
     }
}

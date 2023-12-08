package Middle.Value.BasicBlock;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.ArrayType;
import Middle.Type.IntType;
import Middle.Type.PointerType;
import Middle.Type.ValueType;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;
import SyntaxTree.DeclNode;
import SyntaxTree.StmtNode;

import java.util.*;

public class BasicBlock extends LlvmIrValue {
    private LinkedList<Instruction> instructions;
    private ArrayList<Br> continues = new ArrayList<>();
    private ArrayList<Br> breaks = new ArrayList<>();
    private HashMap<String,LlvmIrValue> define = new HashMap<>();

    public BasicBlock(String name, ValueType valueType) {
        super(name,valueType);
        instructions = new LinkedList<>();
    }

    public void addInstructions(ArrayList<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            this.instructions.add(instruction);
        }
    }

    public void addOneInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }

    public void addAContinue(Br br) {
        continues.add(br);
    }

    public void addABreak(Br br) {
        breaks.add(br);
    }

    public void setBC(LlvmIrValue forBreak, LlvmIrValue forContinue) {
        if (breaks.size() != 0) {
            for (Br b : breaks) {
                b.setJump(forBreak);
            }
        }
        if (continues.size() != 0) {
            for (Br b : continues) {
                b.setJump(forContinue);
            }
        }
        breaks = new ArrayList<>();
        continues = new ArrayList<>();
    }

    public String midOutput() {
        String s = "";
        if (!super.getName().equals("-1")) {
            for (Instruction instruction : instructions) {
                s = s + instruction.midOutput();
            }
        }
        //System.out.println("block name:" + super.getName());
        return s;
    }

    //以下为优化部分
    public HashMap<String,LlvmIrValue> getAndSetAllocaDefine() {
        HashMap<String,LlvmIrValue> hm = new HashMap<>();
        for(Instruction instruction : instructions) {
            if (instruction instanceof Alloca) {
                hm.put(instruction.getName(),instruction);
            }
        }
        return hm;
    }

    public HashMap<String,LlvmIrValue> setStoreDefine(HashMap<String,LlvmIrValue> allAlloca) {
        for (Instruction instruction : instructions) {
            if (instruction instanceof Store) {
                LlvmIrValue right = ((Store) instruction).getRightValue();
                if (allAlloca.containsKey(right.getName())) {
                    if (!define.containsKey(right.getName())) {
                        define.put(right.getName(),right);
                    }
                }
            }
        }
        return define;
    }

    public Boolean defineKey(String key) {
        return define.containsKey(key);
    }

    public void insertPhi(Phi phi) {
        this.instructions.add(1,phi);
    }

    /*public ArrayList<Instruction> fillPhi(HashMap<String,Stack<LlvmIrValue>> alloc,BasicBlock parent) {
        ArrayList<Instruction> t = new ArrayList<>();
        for(Instruction instruction : instructions) {
            if (instruction instanceof Phi) {
                if (alloc.containsKey(instruction.getName())) {
                    ((Phi) instruction).addValue(alloc.get(instruction.getName()).peek(),parent);
                    t.add(instruction);
                }
            }
        }
        return t;
    }*/

    /*public HashMap<String,Stack<LlvmIrValue>> rename(HashMap<String,Stack<LlvmIrValue>> alloca) {
        Iterator<Instruction> iterator = instructions.iterator();
        int cnt = 0;
        while(iterator.hasNext()) {
            Instruction instruction = iterator.next();
            if (instruction instanceof Alloca) { //数组依然要用内存
                if (instruction.getType() instanceof IntType || instruction.getType() instanceof PointerType) {
                    Stack<LlvmIrValue> alloStack = new Stack<>();
                    alloStack.add(new LlvmIrValue("0",new IntType(32)));
                    alloca.put(instruction.getName(),alloStack);
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof Load) {
                LlvmIrValue llvmIrValue = instruction.getOperand().get(0);
                if (alloca.containsKey(llvmIrValue.getName())) {
                    System.out.println(llvmIrValue.getName());
                    System.out.println("name:" + instruction.getName());
                    LlvmIrValue replace = alloca.get(llvmIrValue.getName()).peek();
                    changeLoad(instruction.getName(), replace,cnt);
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof  Store) {
                Store is = (Store) instruction;
                if (alloca.containsKey(is.getRightValue().getName())) {
                    alloca.get(is.getRightValue().getName()).add(is.getLeftValue());
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof Phi) {
                System.out.println(instruction.getName());
                for (String k : alloca.keySet()) {
                    System.out.println(k);
                }
                if (alloca.containsKey(instruction.getName())) {
                    alloca.get(instruction.getName()).add(instruction);
                } else {
                    Stack<LlvmIrValue> alloStack = new Stack<>();
                    alloStack.add(instruction);
                    alloca.put(instruction.getName(),alloStack);
                }
            }
            cnt++;
        }
        return alloca;
    }

    public void changeLoad(String name,LlvmIrValue replace,int cnt) {
        for (int i = cnt + 1;i < instructions.size();i++) {
            Instruction instruction = instructions.get(i);
            ArrayList<LlvmIrValue> operands = instruction.getOperand();
            for (LlvmIrValue l : operands) {
                if (l.getName().equals(name)) {
                    instruction.change(name, replace);
                }
            }
        }
    }*/
}

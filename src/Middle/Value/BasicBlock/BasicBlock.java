package Middle.Value.BasicBlock;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;

import java.util.*;

public class BasicBlock extends LlvmIrValue {
    private LinkedList<Instruction> instructions;
    private ArrayList<Br> continues = new ArrayList<>();
    private ArrayList<Br> breaks = new ArrayList<>();
    private HashMap<String,LlvmIrValue> define = new HashMap<>();
    private ArrayList<String> prev = new ArrayList<>();
    private ArrayList<String> next = new ArrayList<>();
    private HashSet<LlvmIrValue> def = new HashSet<>();
    private HashSet<LlvmIrValue> use = new HashSet<>();
    private HashSet<LlvmIrValue> in = new HashSet<>();
    private HashSet<LlvmIrValue> out = new HashSet<>();
    private ArrayList<BasicBlock> domain = new ArrayList<>();

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

    public void setPrev(ArrayList<String> prev) {
        this.prev = prev;
    }

    public void setNext(ArrayList<String> next) {
        this.next = next;
    }

    public ArrayList<String> getNext() {
        return this.next;
    }

    public ArrayList<String> getPrev() {
        return this.prev;
    }

    public void setDef(LlvmIrValue def) {
        if (!this.use.contains(def)) {
            this.def.add(def);
        }
    }

    public void setUse(LlvmIrValue use) {;
        if (!this.def.contains(use)) {
            if (use.getName().charAt(0) == '%' || use.getName().charAt(0) == '@') {
                this.use.add(use);
            }
        }
    }

    public HashSet<LlvmIrValue> getDef() {
        return this.def;
    }

    public HashSet<LlvmIrValue> getUse() {
        return this.use;
    }

    public void setIn(HashSet<LlvmIrValue> in) {
        this.in = in;
    }

    public void setOut(HashSet<LlvmIrValue> out) {
        this.out = out;
    }

    public HashSet<LlvmIrValue> getIn() {
        return in;
    }

    public HashSet<LlvmIrValue> getOut() {
        return out;
    }

    public void addDomain(BasicBlock b) {
        domain.add(b);
    }

    public ArrayList<BasicBlock> getDomain() {
        return domain;
    }
}

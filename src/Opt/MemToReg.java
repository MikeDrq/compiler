package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Type.PhiType;
import Middle.Type.PointerType;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Func.FuncCnt;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;

import java.util.*;

public class MemToReg {
    public LlvmIrModule llvmIrModule;
    private ArrayList<BasicBlock> basicBlocks;
    private HashMap<String,ArrayList<String>> next; //直接后继
    private HashMap<String,ArrayList<String>> prev;  //所有前驱
    private HashMap<String, HashSet<String>> basicDomain; //支配
    private HashMap<String,String> iDom;//直接支配
    private HashMap<String, LlvmIrValue> allStore;
    private HashMap<String, HashSet<String>> domainFrontier; //支配边界
    private HashMap<String,Stack<LlvmIrValue>> alloca;
    private HashMap<String,Boolean> isArrive;
    private ArrayList<Instruction> allPhi;
    private FuncCnt funcCnt;

    public MemToReg(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
    }

    public Boolean isContainBb(ArrayList<BasicBlock> defines,BasicBlock bb) {
        for (BasicBlock b : defines) {
            if (b.getName().equals(bb.getName())) {
                return true;
            }
        }
        return false;
    }

    public BasicBlock findBasicBlock(String s) {
        for (BasicBlock basicBlock : basicBlocks) {
            if (basicBlock.getName().equals(s)) {
                return basicBlock;
            }
        }
        System.out.println("should no get here!");
        return null;
    }

    public void doMemToReg() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func:funcs) {
            System.out.println("-----------");
            this.funcCnt = func.getFuncCnt();
            this.basicBlocks = func.getBasicBlocks();
            this.iDom = new HashMap<>();
            this.next = new HashMap<>();
            this.prev = new HashMap<>();
            this.domainFrontier = new HashMap<>();
            this.basicDomain = new HashMap<>();
            this.alloca = new HashMap<>();
            this.isArrive = new HashMap<>();
            this.allPhi = new ArrayList<>();
            Iterator<BasicBlock> basicBlockIterator = basicBlocks.iterator();
            while (basicBlockIterator.hasNext()){
                BasicBlock basicBlock = basicBlockIterator.next();
                if (basicBlock.getName().equals("-1")) {
                    basicBlockIterator.remove();
                }
            }
            buildCFG();
            func.setNext(next);
            func.setPrev(prev);
            calculateDomain(); //basicDomain 基本支配
            calculateIDom(); //iDom 计算直接支配
            calculatesDom(); //basicDomain 计算严格支配
            calculateDomainFrontier(); //计算支配边界
            fillDefine();
            insertPhi();
            HashMap<String,ArrayList<String>> tree = new HashMap<>();
            String init = buildDomainTree(tree);
            for (String key : iDom.keySet()) {
                System.out.println(iDom.get(key) + " dominated by " + key);
            }
            walkTree(init,tree);
            for (Instruction instruction : allPhi) {  //phi重命名
                instruction.changeName(((Phi) instruction).getChangeName());
            }

        }
    }

    public void buildCFG() {
        for (BasicBlock basicBlock : basicBlocks) {
            isArrive.put(basicBlock.getName(),false);
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            for (Instruction instruction : instructions) {
                if (instruction instanceof Br) {
                    Br br = (Br) instruction;
                    ArrayList<String> t = new ArrayList<>();
                    if (br.getJumpWithNoCondition()) {
                        String num = br.getJumpName();
                        t.add(num);
                        next.put(basicBlock.getName(),t);
                    } else {
                        t.add(br.getTrueLabel().getName());
                        t.add(br.getFalseLabel().getName());
                        next.put(basicBlock.getName(),t);
                    }
                    break;
                }
            }
        }

        String first = basicBlocks.get(0).getName();//第一个块的名字
        ArrayList<String> t = new ArrayList<>();
        t.add(first);
        prev.put(first,t);
        dfs(first);
        Iterator<String> iterator = next.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            if (!isArrive.get(key)) {
                iterator.remove();
            }
        }
        Iterator<BasicBlock> bi = basicBlocks.iterator();
        while(bi.hasNext()) {
            String name = bi.next().getName();
            if (!isArrive.get(name)) {
                bi.remove();
            }
        }
    }

    public void calculateDomain() { //构建支配关系
        for (BasicBlock basicBlock : basicBlocks) { //initialize
            HashSet<String> t = new HashSet<>();
            if (!prev.containsKey(basicBlock.getName())) {
                t.add(basicBlock.getName());
            }
            basicDomain.put(basicBlock.getName(),t);
        }
        int flag;
        do {
            flag = 0;
            for (BasicBlock bs : basicBlocks) {
                String s = bs.getName();
                if (prev.containsKey(s)) {
                    ArrayList<String> prevs = prev.get(s);
                    HashSet<String> cup = new HashSet<>();
                    for (String ps : prevs) {
                        cup.addAll(basicDomain.get(ps));
                    }
                    for (String ps : prevs) {
                        if (basicDomain.get(ps).size() != 0) {
                            cup.retainAll(basicDomain.get(ps));
                        }
                    }
                    cup.add(s);
                    HashSet<String> origin = basicDomain.get(s);
                    if (!cup.equals(origin)) {
                        basicDomain.put(s, cup);
                        flag = 1;
                    }
                }
            }
        } while(flag == 1);

    }

    public void calculatesDom() {
        for (String key: basicDomain.keySet()) {
            Iterator<String> iterator = basicDomain.get(key).iterator();
            while(iterator.hasNext()) {
                if (iterator.next().equals(key)) {
                    iterator.remove();
                }
            }
        }
    }

    public void calculateIDom() {
        for (String key : basicDomain.keySet()) {
            HashSet<String> domains = basicDomain.get(key);
            HashSet<String> cpy = new HashSet<>(domains);
            cpy.remove(key);
            if (domains.size() >= 2) {
                for (String s : domains) {
                    if (cpy.equals(basicDomain.get(s))) {
                        iDom.put(key, s);
                        break;
                    }
                }
            } else {
                for (String s : domains) {
                    iDom.put(key, s);
                }
            }
        }
    }

    public void calculateDomainFrontier() {
        for (String key : next.keySet()) {
            ArrayList<String> next_node = next.get(key);
            for (String s : next_node) {
                String temp = key;
                while (!basicDomain.get(s).contains(temp)) {
                    if (!domainFrontier.containsKey(temp)) {
                        HashSet<String> hs = new HashSet<>();
                        hs.add(s);
                        domainFrontier.put(temp, hs);
                    } else {
                        domainFrontier.get(temp).add(s);
                    }
                    if (iDom.containsKey(temp) && !iDom.get(temp).equals(temp)) {
                        temp = iDom.get(temp);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void dfs(String cur) {
        if (isArrive.get(cur)) {
            return;
        }
        isArrive.put(cur,true);
        if (!next.containsKey(cur)) {
            return;
        }
        ArrayList<String> nexts = next.get(cur);

        for (String s : nexts) {
            if (prev.containsKey(s)) {
                prev.get(s).add(cur);
            } else {
                ArrayList<String> t = new ArrayList<>();
                t.add(cur);
                prev.put(s, t);
            }
            dfs(s);
        }
    }

    public HashMap<String, LlvmIrValue> fillDefine() {
        HashMap<String,LlvmIrValue> define = new HashMap<>();
        for (BasicBlock basicBlock : basicBlocks) {
            HashMap<String,LlvmIrValue> ta = basicBlock.getAndSetAllocaDefine();
            for (String s :ta.keySet()) {
                if (define.containsKey(s)) {
                    System.out.println("no!!!");
                } else {
                    define.put(s,ta.get(s));
                }
            }
        }
        allStore = new HashMap<>();
        for (BasicBlock basicBlock : basicBlocks) {
            HashMap<String,LlvmIrValue> one = basicBlock.setStoreDefine(define);
            for (String s :one.keySet()) {
                if (!allStore.containsKey(s)) {
                    allStore.put(s,one.get(s));
                }
            }
        }
        return allStore;
    }

    public void insertPhi() {
        for (String key : allStore.keySet()) {
            HashMap<String,BasicBlock> inserts = new HashMap<>(); //插入phi的block
            Stack<BasicBlock> defines = new Stack<>(); //定义该变量的block
            ArrayList<BasicBlock> db = new ArrayList<>();
            for (BasicBlock basicBlock : basicBlocks) {
                if (basicBlock.defineKey(key)) {
                    defines.push(basicBlock);
                    db.add(basicBlock);
                }
            }
            while(!defines.empty()) {
                BasicBlock b = defines.pop(); //X块
                if (domainFrontier.containsKey(b.getName())) {
                    HashSet<String> bn = domainFrontier.get(b.getName()); //Y块
                    for (String s : bn) {
                        if (!inserts.containsKey(s)) {
                            BasicBlock bb = findBasicBlock(s);
                            Phi phi = new Phi( key, new PhiType(),"%v_"+ funcCnt.getCnt());
                            bb.insertPhi(phi);
                            inserts.put(s,bb);
                            if (!isContainBb(db,bb)) {
                                defines.push(bb);
                            }
                        }
                    }
                }
            }
        }
    }

    public String buildDomainTree(HashMap<String,ArrayList<String>> tree) {
        String init = "";
        for (String key : iDom.keySet()) {
            String value = iDom.get(key);
            if (key.equals(value)) {
                init = value;
            } else {
                if (tree.containsKey(value)) {
                    tree.get(value).add(key);
                } else {
                    ArrayList<String> a = new ArrayList<>();
                    a.add(key);
                    tree.put(value, a);
                }
            }
        }
        return init;
    }

    public void doWalk(String cur,HashMap<String,ArrayList<String>> tree) {
        BasicBlock bb = findBasicBlock(cur);
        System.out.println("<<<<<<<<<< this is " + cur);
        HashMap<String,Integer> addInStack = rename(bb);
        if (cur.equals("56")) {
            System.out.println("stop");
        }
        if (next.containsKey(cur)) {
            for(String s : next.get(cur)) {
                BasicBlock cb = findBasicBlock(s);
                allPhi.addAll(fillPhi(bb,cb));
            }
        }
        if (tree.containsKey(cur)) {
            for (String s : tree.get(cur)) {
                doWalk(s, tree);
            }
        }
        for (String key : addInStack.keySet()) {
            int num = addInStack.get(key);
            System.out.println("pop "+ key + " : " + num);
            for (int i = 0;i < num;i++) {
                alloca.get(key).pop();
            }
        }
    }

    public void walkTree(String init,HashMap<String,ArrayList<String>> tree) {
        doWalk(init,tree);
    }

    public HashMap<String,Integer> rename(BasicBlock bb) {
        LinkedList<Instruction> instructions = bb.getInstructions();
        Iterator<Instruction> iterator = instructions.iterator();
        int cnt = 0;
        HashMap<String,Integer> insStackCount = new HashMap<>();
        while(iterator.hasNext()) {
            Instruction instruction = iterator.next();
            if (instruction instanceof Alloca) { //数组依然要用内存
                if (instruction.getType() instanceof IntType || instruction.getType() instanceof PointerType) {
                    if (alloca.containsKey(instruction.getName())) {
                        alloca.get(instruction.getName()).push(new LlvmIrValue("0",new IntType(32)));
                    } else {
                        Stack<LlvmIrValue> alloStack = new Stack<>();
                        alloStack.push(new LlvmIrValue("0", new IntType(32)));
                        alloca.put(instruction.getName(), alloStack);
                    }
                    if (insStackCount.containsKey(instruction.getName())) {
                        int num = insStackCount.get(instruction.getName());
                        insStackCount.put(instruction.getName(),num + 1);
                    } else {
                        insStackCount.put(instruction.getName(),1);
                    }
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof Load) {
                LlvmIrValue llvmIrValue = instruction.getOperand().get(0);
                if (alloca.containsKey(llvmIrValue.getName())) {
                    LlvmIrValue replace = alloca.get(llvmIrValue.getName()).peek();
                    changeLoad(instruction.getName(), replace,cnt,instructions);
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof  Store) {
                Store is = (Store) instruction;
                if (alloca.containsKey(is.getRightValue().getName())) {
                    alloca.get(is.getRightValue().getName()).push(is.getLeftValue());
                    if (insStackCount.containsKey(is.getRightValue().getName())) {
                        int num = insStackCount.get(is.getRightValue().getName());
                        insStackCount.put(is.getRightValue().getName(),num + 1);
                    } else {
                        insStackCount.put(is.getRightValue().getName(),1);
                    }
                    iterator.remove();
                    cnt--;
                }
            } else if (instruction instanceof Phi) {
                if (alloca.containsKey(instruction.getName())) {
                    alloca.get(instruction.getName()).push(instruction);
                } else {
                    Stack<LlvmIrValue> alloStack = new Stack<>();
                    alloStack.push(instruction);
                    alloca.put(instruction.getName(),alloStack);
                }
                if (insStackCount.containsKey(instruction.getName())) {
                    int num = insStackCount.get(instruction.getName());
                    insStackCount.put(instruction.getName(),num + 1);
                } else {
                    insStackCount.put(instruction.getName(),1);
                }
                System.out.println("add 1: " + instruction.getName() +" " + insStackCount.get(instruction.getName()));
            }
            cnt++;
        }
        return insStackCount;
    }

    public void changeLoad(String name,LlvmIrValue replace,int cnt,LinkedList<Instruction> instructions) {
        for (int i = cnt + 1;i < instructions.size();i++) {
            Instruction instruction = instructions.get(i);
            ArrayList<LlvmIrValue> operands = instruction.getOperand();
            for (LlvmIrValue l : operands) {
                if (l.getName().equals(name)) {
                    instruction.change(name, replace);
                }
            }
        }
    }

    public ArrayList<Instruction> fillPhi(BasicBlock parent,BasicBlock cur) {
        LinkedList<Instruction> instructions = cur.getInstructions();
        ArrayList<Instruction> t = new ArrayList<>();
        for(Instruction instruction : instructions) {
            if (instruction instanceof Phi) {
                if (instruction.getName().equals("%v_1")) {
                    System.out.println("hi");
                }
                if (instruction.getName().equals("%v_31")) {
                    System.out.println("good");
                }
                if (alloca.containsKey(instruction.getName())) {
                    if (alloca.get(instruction.getName()).empty()) {
                        ((Phi) instruction).addValue(new LlvmIrValue("0",new IntType(32)), parent);
                    } else {
                        ((Phi) instruction).addValue(alloca.get(instruction.getName()).peek(), parent);
                    }
                    t.add(instruction);
                } else {
                    ((Phi) instruction).addValue(new LlvmIrValue("0",new IntType(32)), parent);
                }
            }
        }
        return t;
    }
}

package Middle.Value.Func;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.FuncType;
import Middle.Type.PhiType;
import Middle.Type.PointerType;
import Middle.Type.ValueType;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Instruction.AllInstructions.Alloca;
import Middle.Value.Instruction.AllInstructions.Br;
import Middle.Value.Instruction.AllInstructions.Phi;
import Middle.Value.Instruction.InstructionType;
import Middle.Value.Instruction.Instruction;

import java.util.*;

public class Func extends LlvmIrValue {

    private ArrayList<LlvmIrValue> params = new ArrayList<>();
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    private HashMap<String,ArrayList<String>> next = new HashMap<>(); //直接后继
    private HashMap<String,ArrayList<String>> prev = new HashMap<>();  //所有前驱
    private HashMap<String, HashSet<String>> basicDomain = new HashMap<>(); //支配
    private HashMap<String,String> iDom = new HashMap<>();//直接支配
    private HashMap<String,LlvmIrValue> allStore = new HashMap<>();
    private HashMap<String, HashSet<String>> domainFrontier = new HashMap<>(); //支配边界
    private HashMap<String,Stack<LlvmIrValue>> alloca = new HashMap<>();
    private HashMap<String,Boolean> isArrive = new HashMap<>();
    private ArrayList<Instruction> allPhi = new ArrayList<>();
    private int phiCnt;
    public Func(String name,ValueType valueType) {
        super(name,valueType);
        params = ((FuncType) super.getType()).getParams();
        phiCnt = 0;
    }

    public void addBasicBlocks(ArrayList<BasicBlock> basicBlocks) {
        if (basicBlocks != null) {
            for (BasicBlock basicBlock : basicBlocks) {
                this.basicBlocks.add(basicBlock);
            }
        }
    }

    public ArrayList<LlvmIrValue> getParams() {
        return this.params;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("\ndefine dso_local ").append(super.getType().midOutput()).append(" ").append(super.getName()).append("(");
        for (int i = 0;i < params.size();i++) {
            sb.append(outPutParams(params.get(i)));
            /*s = s + params.get(i).getType().midOutput();
            s = s + " ";
            s = s + params.get(i).getName();*/
            if (i != params.size() - 1) {
                sb.append(",") ;
            }
        }
        sb.append(") #0 {\n");
        for (BasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.midOutput());
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String outPutParams(LlvmIrValue param) {
        StringBuilder sb = new StringBuilder();
        if (param.getDim() == 0) {
            sb.append("i32 ").append(param.getName());
        } else if (param.getDim() == 1) {
            sb.append("i32* ").append(param.getName());
        } else if (param.getDim() == 2) {
            sb.append("[").append(param.getColumn()).append(" x i32]").append(" *").append(param.getName());
        } else {
            System.out.println("error");
        }
        return sb.toString();
    }

    /*以下为优化部分*/
    /*public void calculateDomain() { //构建支配关系
        // 初始化
        for (BasicBlock basicBlock : basicBlocks) {
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

    public Boolean isContainBb(ArrayList<BasicBlock> defines,BasicBlock bb) {
        for (BasicBlock b : defines) {
            if (b.getName().equals(bb.getName())) {
                return true;
            }
        }
        return false;
    }

    public void insertPhi() {
        for (String key : allStore.keySet()) {
            HashMap<String,BasicBlock> inserts = new HashMap<>(); //插入phi的block
            ArrayList<BasicBlock> defines = new ArrayList<>(); //定义该变量的block
            for (BasicBlock basicBlock : basicBlocks) {
                if (basicBlock.defineKey(key)) {
                    defines.add(basicBlock);
                }
            }
            ListIterator<BasicBlock> iterator = defines.listIterator();
            while(iterator.hasNext()) {
                BasicBlock b = iterator.next(); //X块
                iterator.remove();
                if (domainFrontier.containsKey(b.getName())) {
                    HashSet<String> bn = domainFrontier.get(b.getName()); //Y块
                    for (String s : bn) {
                        if (!inserts.containsKey(s)) {
                            Phi phi = new Phi( key, new PhiType(),"%p_"+phiCnt);
                            phiCnt++;
                            BasicBlock bb = findBasicBlock(s);
                            bb.insertPhi(phi);
                            inserts.put(s,bb);
                            if (!isContainBb(defines,bb)) {
                                iterator.add(bb);
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

    public void rename(BasicBlock bb) {
        bb.rename(alloca);
    }

    public void doWalk(String cur,HashMap<String,ArrayList<String>> tree) {
        BasicBlock bb = findBasicBlock(cur);
        if (cur.equals("57")) {
            System.out.println("gggod");
        }
        rename(bb);
        if (next.containsKey(cur)) {
            if (cur.equals("29")) {
                System.out.println("hhhh");
            }
            for(String s : next.get(cur)) {
                BasicBlock cb = findBasicBlock(s);
                allPhi.addAll(cb.fillPhi(alloca,bb));
            }
        }
        if (tree.containsKey(cur)) {

            for (String s : tree.get(cur)) {
                doWalk(s, tree);
            }
        }

    }

    public void walkTree(String init,HashMap<String,ArrayList<String>> tree) {
        doWalk(init,tree);
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
        Iterator<BasicBlock> basicBlockIterator = basicBlocks.iterator();
        while (basicBlockIterator.hasNext()){
            BasicBlock basicBlock = basicBlockIterator.next();
            if (basicBlock.getName().equals("-1")) {
                basicBlockIterator.remove();
            }
        }
        buildCFG();
        calculateDomain(); //basicDomain 基本支配
        calculateIDom(); //iDom 计算直接支配
        calculatesDom(); //basicDomain 计算严格支配
        calculateDomainFrontier(); //计算支配边界
        fillDefine();
        insertPhi();
        HashMap<String,ArrayList<String>> tree = new HashMap<>();
        String init = buildDomainTree(tree);
        walkTree(init,tree);
        for (Instruction instruction : allPhi) {  //phi重命名
           instruction.changeName(((Phi) instruction).getChangeName());
        }
    }

    public String domainOutput() {
        String s = "\n";
        for (String key : next.keySet()) {
            if (next.get(key).size() == 1) {
                s = s + key + " next " + next.get(key).get(0) + "\n";
            } else if (next.get(key).size() == 2) {
                s = s + key + " next " + "true: " + next.get(key).get(0) + ",false: " + next.get(key).get(1) + "\n";
            }
        }
        s = s + "prev:\n";
        for (String key : prev.keySet()) {
            s = s + key + " prev are: ";
            for (String ps : prev.get(key)) {
                s = s + " " + ps + " ";
            }
            s = s + "\n";
        }*/
        /*
        s = s + "domain:\n";
        for (String key : basicDomain.keySet()) {
            s = s + key + "dominated by: ";
            for (String t : basicDomain.get(key)) {
                s = s + " " + t + " ";
            }
            s = s + "\n";
        }
        s = s + "\n";
        s = s + "strict domain:\n";
        for (String key : iDom.keySet()) {
            s = s + key + "strict domains: " + iDom.get(key);
            s = s + "\n";
        }
        s = s + "\n";
        s = s + "frontier:\n";
        for (String key : domainFrontier.keySet()) {
            s = s + key + "frontiers :";
            for (String t : domainFrontier.get(key)) {
                s = s + " " + t + " ";
            }
            s = s + "\n";
        }
        s = s + "\n";
        return s;
    }*/
}

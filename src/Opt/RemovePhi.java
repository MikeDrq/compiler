package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Type.LabelType;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.BasicBlock.BasicBlockCnt;
import Middle.Value.Func.Func;
import Middle.Value.Func.FuncCnt;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;

import java.util.*;

public class RemovePhi {
    private LlvmIrModule llvmIrModule;
    private BasicBlockCnt basicBlockCnt;
    private HashMap<String,BasicBlock> basicBlockIndex;
    private HashMap<LlvmIrValue,Integer> varReg;

    public RemovePhi(LlvmIrModule llvmIrModule,BasicBlockCnt basicBlockCnt) {
        this.llvmIrModule = llvmIrModule;
        this.basicBlockCnt = basicBlockCnt;
    }

    public void doRemovePhi() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func : funcs) {
            varReg = func.getVarReg();
            insertPC(func);
            removePC(func);
        }
    }

    private void createBasicBlockIndex(ArrayList<BasicBlock> basicBlocks) {
        basicBlockIndex = new HashMap<>();
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlockIndex.put(basicBlock.getName(),basicBlock);
        }
    }

    private Boolean containPhi(BasicBlock basicBlock) {
        LinkedList<Instruction> instructions = basicBlock.getInstructions();
        for (Instruction instruction : instructions) {
            if (instruction instanceof Phi) {
                return true;
            }
        }
        return false;
    }

    private void insertPC(Func func) {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>(func.getBasicBlocks());
        createBasicBlockIndex(basicBlocks);
        for (BasicBlock basicBlock : basicBlocks) {
            if (!containPhi(basicBlock)) {
                continue;
            }
            ArrayList<String> blockPrev = new ArrayList<>(basicBlock.getPrev());
            ArrayList<String> blockNext = new ArrayList<>(basicBlock.getNext());
            ArrayList<Pc> pcList = new ArrayList<>();
            for (String s : blockPrev) {
                if (!s.equals(basicBlock.getName())) { //入口的prev包含他本身，continue
                    pcList.add(new Pc("%v_" + func.getFuncCnt().getCnt(),new IntType(32),s));
                }
            }
            for (int i = 0;i < blockPrev.size();i++) {
                if (!blockPrev.get(i).equals(basicBlock.getName())) {
                    Pc pc = pcList.get(i);
                    String bbName = blockPrev.get(i);
                    BasicBlock bb = basicBlockIndex.get(bbName);
                    if (bb.getNext().size() == 1) {
                        simpleInsert(bb, pc);
                    } else {
                        complexInsert(bb,basicBlock,pc,func);
                    }
                }
            }

            Iterator<Instruction> iterator = basicBlock.getInstructions().iterator();
            while(iterator.hasNext()) {
                Instruction instruction = iterator.next();
                if (instruction instanceof Phi) {
                    for (int i = 0;i < pcList.size();i++) {
                        Pc pc = pcList.get(i);
                        String orgBelong = pc.getBbBelong();
                        ArrayList<LlvmIrValue> sources = ((Phi) instruction).getSource();
                        ArrayList<LlvmIrValue> values = ((Phi) instruction).getValues();
                        for (int j = 0;j < sources.size();j++) {
                            if (sources.get(j).getName().equals(orgBelong)) {
                                pc.addValue(instruction,values.get(j));
                                break;
                            }
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }

    private void simpleInsert(BasicBlock bb,Pc pc) {
        LinkedList<Instruction> instructions = bb.getInstructions();
        int size = instructions.size();
        instructions.add(size - 1,pc);
    }

    private void complexInsert(BasicBlock prevBb,BasicBlock curBb,Pc pc,Func func) {
        LinkedList<Instruction> instructions = prevBb.getInstructions();
        Instruction lastIns = instructions.getLast();
        if (!(lastIns instanceof Br)) {
            System.out.println("error,should be br");
        } else {
            int num = basicBlockCnt.getCnt();
            BasicBlock basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
            ArrayList<BasicBlock> t = new ArrayList<>();
            t.add(basicBlock);
            func.addBasicBlocks(t);
            basicBlock.addOneInstruction(new Label(String.valueOf(num),new LabelType(num))); //新建一个块，
            basicBlock.addOneInstruction(pc);
            LlvmIrValue trueLabel = ((Br) lastIns).getTrueLabel();
            LlvmIrValue falseLabel = ((Br) lastIns).getFalseLabel();
            LlvmIrValue jump = null;
            if (trueLabel.getName().equals(curBb.getName())) {
                jump = trueLabel;
                ((Br) lastIns).setTrueLabel(basicBlock);
            } else if (falseLabel.getName().equals(curBb.getName())) {
                jump = falseLabel;
                ((Br) lastIns).setFalseLabel(basicBlock);
            } else {
                System.out.println("error,didn't contain");
            }
            Br br = new Br("br",null);
            br.setJump(jump);
            basicBlock.addOneInstruction(br);
            //修改prev，next
            ArrayList<String> next = new ArrayList<>();
            ArrayList<String> prev = new ArrayList<>();
            next.add(jump.getName()); //
            prev.add(prevBb.getName());
            basicBlock.setNext(next);
            basicBlock.setPrev(prev);

            prevBb.getNext().set(prevBb.getNext().indexOf(next.get(0)),basicBlock.getName());

            BasicBlock nextBasicBlock = basicBlockIndex.get(jump.getName());
            prev = nextBasicBlock.getPrev();
            for (int i = 0;i < next.size();i++) {
                if (prev.get(i).equals(jump.getName())) {
                    prev.set(i,jump.getName());
                }
            }
        }
    }

    private void removePC(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            int size = instructions.size();
            if (size >= 2 && instructions.get(size - 2) instanceof Pc) {
                LinkedList<MidMove> moves = getMoves(instructions.get(size - 2),func.getFuncCnt());
                instructions.remove(size-2);
                for (MidMove move : moves) {
                    instructions.add(instructions.size() - 1,move);
                }
            }
        }
    }

    private LinkedList<MidMove> getMoves(Instruction instruction, FuncCnt funcCnt) {
        LinkedList<MidMove> moves = new LinkedList<>();
        ArrayList<LlvmIrValue> phis = ((Pc)instruction).getPhis();
        ArrayList<LlvmIrValue> value = ((Pc) instruction).getValues();
        LinkedList<MidMove> temp = new LinkedList<>();
        HashSet<LlvmIrValue> moveRec = new HashSet<>();
        for (int i = 0;i < value.size();i++) {
            MidMove move = new MidMove("",null,value.get(i),phis.get(i));
            moves.add(move);
        }
        for (int i = 0;i < moves.size();i++) {
            String s = moves.get(i).getDst().getName();
            if (!isConstant(s) && !moveRec.contains(moves.get(i).getDst())) {
                int flag = 0;
                for (int j = i + 1; j < moves.size(); j++) {
                    if (moves.get(j).getSrc().getName().equals(s)) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 1) {
                    int num = funcCnt.getCnt();
                    LlvmIrValue llvmIrValue = new LlvmIrValue("%v_" + num, new IntType(num));
                    MidMove move = new MidMove("", null, moves.get(i).getDst(),llvmIrValue);
                    temp.add(move);
                    for (MidMove item : moves) {
                        if (item.getSrc().getName().equals(s)) {
                            item.setSrc(llvmIrValue);
                        }
                    }
                }
                moveRec.add(moves.get(i).getDst());
            }
        }
        moveRec = new HashSet<>();
        for(int i = moves.size() - 1;i >=0 ;i --) {
            String s = moves.get(i).getSrc().getName();
            if (!isConstant(s) && !moveRec.contains(moves.get(i).getSrc())) {
                int flag = 0;
                for (int j = 0;j < i;j++) {
                    if (varReg != null && varReg.containsKey(moves.get(i).getSrc())) {
                        if (varReg.containsKey(moves.get(j).getDst()) && varReg.get(moves.get(j).getDst()).equals(varReg.get(moves.get(i).getSrc()))) {
                            flag = 1;
                            System.out.println("---" + moves.get(j).getDst().getName());
                            break;
                        }
                    }
                }
                if (flag == 1) {
                    int num = funcCnt.getCnt();
                    LlvmIrValue llvmIrValue = new LlvmIrValue("%v_" + num, new IntType(num));
                    MidMove move = new MidMove("", null, moves.get(i).getSrc(),llvmIrValue);
                    temp.add(move);
                    for (MidMove item : moves) {
                        if (item.getSrc().getName().equals(moves.get(i).getSrc().getName())) {
                            item.setSrc(llvmIrValue);
                        }
                    }
                }
                moveRec.add(moves.get(i));
            }
        }
        for (MidMove move : temp) {
            moves.addFirst(move);
        }
        return moves;
    }

    private Boolean isConstant(String name) {
        if (name.charAt(0) == '%' || name.charAt(0) == '@') {
            return false;
        }
        return true;
    }

}

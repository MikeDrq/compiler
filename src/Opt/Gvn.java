package Opt;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Instruction.AllInstructions.Calculate;
import Middle.Value.Instruction.Instruction;
import Middle.Value.Instruction.InstructionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Gvn {
    public LlvmIrModule llvmIrModule;

    public Gvn(LlvmIrModule llvmIrModule) {
        this.llvmIrModule = llvmIrModule;
    }

    public void doGvn() {
        ArrayList<Func> funcs = llvmIrModule.getFunctions();
        for (Func func : funcs) {
            simplifyCalculate(func);
        }
    }

    public Boolean isConstant(String name) {
        if (name.charAt(0) == '%' || name.charAt(0) == '@') {
            return false;
        }
        return true;
     }

     public void changeUse(LlvmIrValue l,ArrayList<BasicBlock> basicBlocks,String name) {
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            for (Instruction instruction : instructions) {
                ArrayList<LlvmIrValue> operands = instruction.getOperand();
                for (LlvmIrValue operand : operands) {
                    if (operand.getName().equals(name)) {
                        instruction.change(name,l);
                    }
                }
            }
        }
     }

     public Boolean oneContant(LlvmIrValue constant,LlvmIrValue normal,ArrayList<BasicBlock> basicBlocks,Instruction instruction,int order) {
         InstructionType instructionType = ((Calculate) instruction).getInstructionType();
        if (constant.getName().equals("0")) {
            if (instructionType == InstructionType.mul || instructionType == InstructionType.sdiv || instructionType == InstructionType.srem) {
                changeUse(new LlvmIrValue(String.valueOf(0), new IntType(32)), basicBlocks, instruction.getName());
                return true;
            } else if (instructionType == InstructionType.add ){
                changeUse(normal,basicBlocks,instruction.getName());
                return true;
            } else if (instructionType == InstructionType.sub) {
                if (order == 2) {
                    changeUse(normal,basicBlocks,instruction.getName());
                    return true;
                }
            }
        } else if (constant.getName().equals("1")) {
            if (instructionType == InstructionType.mul) {
                changeUse(normal,basicBlocks,instruction.getName());
                return true;
            } else if (instructionType == InstructionType.sdiv) {
                if (order == 2) {
                    changeUse(normal,basicBlocks,instruction.getName());
                    return true;
                }
            } else if (instructionType == InstructionType.srem) {
                if (order == 2) {
                    changeUse(new LlvmIrValue(String.valueOf(0), new IntType(32)), basicBlocks, instruction.getName());
                    return true;
                }
            }
        }
        return false;
     }

     public Boolean twoConstant(String name1,String name2,ArrayList<BasicBlock> basicBlocks,Instruction instruction) {
         int a = Integer.parseInt(name1);
         int b = Integer.parseInt(name2);
         int c;
         InstructionType instructionType = ((Calculate) instruction).getInstructionType();
         if (instructionType == InstructionType.add) {
             c = a + b;
             changeUse(new LlvmIrValue(String.valueOf(c),new IntType(32)),basicBlocks,instruction.getName());
             return true;
         } else if (instructionType == InstructionType.sub) {
             c = a - b;
             changeUse(new LlvmIrValue(String.valueOf(c),new IntType(32)),basicBlocks,instruction.getName());
             return true;
         } else if (instructionType == InstructionType.mul) {
             c = a * b;
             changeUse(new LlvmIrValue(String.valueOf(c),new IntType(32)),basicBlocks,instruction.getName());
             return true;
         } else if (instructionType == InstructionType.sdiv) {
             c = a / b;
             changeUse(new LlvmIrValue(String.valueOf(c),new IntType(32)),basicBlocks,instruction.getName());
             return true;
         } else if (instructionType == InstructionType.srem) {
             c = a % b;
             changeUse(new LlvmIrValue(String.valueOf(c),new IntType(32)),basicBlocks,instruction.getName());
             return true;
         }
         return false;
     }

    public void simplifyCalculate(Func func) {
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            LinkedList<Instruction> instructions = basicBlock.getInstructions();
            Iterator<Instruction> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                Instruction instruction = iterator.next();
                if (instruction instanceof Calculate) {
                    LlvmIrValue operand1 = ((Calculate) instruction).getLeft();
                    LlvmIrValue operand2 = ((Calculate) instruction).getRight();
                    String name1 = operand1.getName();
                    String name2 = operand2.getName();
                    if (isConstant(name1) && isConstant(name2)) {
                        if (twoConstant(name1,name2,basicBlocks,instruction)) {
                            iterator.remove();
                        }
                    } else if (isConstant(name1)) {
                        if (oneContant(operand1,operand2,basicBlocks,instruction,1)) {
                            iterator.remove();
                        }
                    } else if (isConstant(name2)) {
                        if (oneContant(operand2,operand1,basicBlocks,instruction,2)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
}

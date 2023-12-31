package Middle.Value.Func;

import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.*;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Instruction.AllInstructions.Alloca;
import Middle.Value.Instruction.AllInstructions.Br;
import Middle.Value.Instruction.AllInstructions.Phi;
import Middle.Value.Instruction.InstructionType;
import Middle.Value.Instruction.Instruction;
import Mips.MipsInstruction.J;

import java.util.*;

public class Func extends LlvmIrValue {

    private ArrayList<LlvmIrValue> params = new ArrayList<>();
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    private HashMap<String,String> iDom = new HashMap<>();
    private HashMap<LlvmIrValue,Integer> varReg;
    private ValueType retValue;

    private FuncCnt funcCnt;
    public Func(String name,ValueType valueType,FuncCnt funcCnt) {
        super(name,valueType);
        params = ((FuncType) super.getType()).getParams();
        retValue = ((FuncType) super.getType()).getRetType();
        this.funcCnt = funcCnt;
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

    public Boolean hasRetValue() {
        if (retValue instanceof IntType) {
            return true;
        }
        return false;
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
    public FuncCnt getFuncCnt() {
        return funcCnt;
    }

    public void setiDom(HashMap<String, String> iDom) {
        this.iDom = iDom;
    }

    public HashMap<String, String> getiDom() {
        return iDom;
    }

    public void addVarReg(HashMap<LlvmIrValue,Integer> varReg) {
        this.varReg = varReg;
    }

    public HashMap<LlvmIrValue, Integer> getVarReg() {
        return varReg;
    }
}

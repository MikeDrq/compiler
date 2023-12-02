package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.FuncType;
import Middle.Type.IntType;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Call extends Instruction {
    private ArrayList<LlvmIrValue> params;
    private LlvmIrValue funcValue;
    private int type;
    private String funcName;
    private LlvmIrValue op;
    private String asci;
    private String c;
    public Call(String name, ValueType valueType, ArrayList<LlvmIrValue> values,LlvmIrValue funcValue){
        super(name,valueType);
        params = values;
        this.funcValue = funcValue;
        type = 0;
    }

    public Call(String name,ValueType valueType,String funcName) {  //getint
        super(name,valueType);
        type = 1;
        this.funcName = funcName;
    }

    public Call(String name,ValueType valueType,String funcName,LlvmIrValue op) { //putint
        super(name,valueType);
        type = 2;
        this.funcName = funcName;
        this.op = op;
    }

    public Call(String name,ValueType valueType,String funcName,char c) { //putch
        super(name,valueType);
        type = 3;
        this.funcName = funcName;
        this.asci = String.valueOf((int) c);
        this.c = String.valueOf(c);
    }

    public int getMark() {
        return this.type;
    }

    public String getChar() {
        return c;
    }

    public LlvmIrValue getOp() {
        return op;
    }

    public ArrayList<LlvmIrValue> getParams() {
        return params;
    }

    public String getFuncName() {
        return this.funcName;
    }

    public LlvmIrValue getFuncValue() {
        return this.funcValue;
    }

    @Override
    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        if (type == 0) {
            FuncType funcType = (FuncType) super.getType();
            if (funcType.getRetType() instanceof IntType) {
                sb.append(super.getName()).append(" = call ").append(funcType.midOutput()).append(" ");
            } else {
                sb.append("call ").append(funcType.midOutput()).append(" ");
            }
            sb.append(funcValue.getName()).append("(");
            for (int i = 0; i < params.size(); i++) {
                sb.append(outPutParam(params.get(i)));
                if (i != params.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");
        } else if (type == 1) {
            sb.append(super.getName()).append(" = call ").
                    append(super.getType().midOutput()).append(" ").append(funcName).append("()\n");
        } else if (type == 2) {
            sb.append("call ").append(super.getType().midOutput()).append(" ").append(funcName).append("(i32 ").append(op.getName()).append(")\n");
        } else if (type == 3) {
            sb.append("call ").append(super.getType().midOutput()).append(" ").append(funcName).append("(i32 ").append(asci).append(")\n");
        }
        return sb.toString();
    }

    private String outPutParam(LlvmIrValue param) {
        StringBuilder sb = new StringBuilder();
        if (param.getDim() == 0) {
            //sb.append(param.getType().midOutput()).append(" ").append(param.getName());
            sb.append("i32 ").append(param.getName());
        } else if (param.getDim() == 1) {
            if (param.getRParamDim() == 1) {
                sb.append("i32* ").append(param.getName());
            } else if (param.getRParamDim() == 0) {
                sb.append("i32 ").append(param.getName());
            } else {
                System.out.println("error");
            }
        } else if (param.getDim() == 2) {
            if (param.getRParamDim() == 2) {
                sb.append("[").append(param.getColumn()).append(" x ").append("i32]* ").append(param.getName());
            } else if (param.getRParamDim() == 1) {
                sb.append("i32* ").append(param.getName());
            } else if (param.getRParamDim() == 0) {
                sb.append("i32 ").append(param.getName());
            } else {
                System.out.println("error");
            }
        }
        return sb.toString();
    }
}

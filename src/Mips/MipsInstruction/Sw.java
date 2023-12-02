package Mips.MipsInstruction;

import Mips.Register;

public class Sw extends MipsInstruction {
    private int regNum;
    private int base;
    private int offset;
    private int flag;
    private String globalVar;

    public Sw(int regNum,int base,int offset) {
        super("Lw");
        this.regNum = regNum;
        this.base = base; //$sp
        this.offset = offset;
        flag = 1; //sw $t0,100($t2)
    }

    public Sw(int regNum,String globalVar) {
        super("Lw");
        this.regNum = regNum;
        this.globalVar = globalVar;
        flag = 0; //sw $t0,a
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        if (flag == 1) {
            sb.append("sw ").append(register.getRegister(regNum)).append(", ")
                    .append(offset).append(", (").append(register.getRegister(base)).append(")\n");
        } else {
            sb.append("sw ").append(register.getRegister(regNum)).append(", ")
                    .append(globalVar).append("\n");
        }
        return sb.toString();
    }
}

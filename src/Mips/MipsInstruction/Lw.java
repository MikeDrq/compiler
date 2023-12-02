package Mips.MipsInstruction;

import Mips.Register;

public class Lw extends MipsInstruction {
    private int regNum;
    private int base;
    private int offset;
    private int flag;
    private String globalName;

    public Lw(int regNum,int base,int offset) {
        super("Lw");
        this.regNum = regNum;
        this.base = base;
        this.offset = offset; //$sp
        flag = 1; //sw $t0,100($t2)
    }

    public Lw(int regNum,String globalName) {
        super("Lw");
        this.regNum = regNum;
        this.globalName = globalName;
        flag = 0; //sw $t0,a
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        if (flag == 1) {
            sb.append("lw ").append(register.getRegister(regNum)).append(", ")
                    .append(offset).append(", (").append(register.getRegister(base)).append(")\n");
        } else {
            sb.append("lw ").append(register.getRegister(regNum)).append(", ")
                    .append(globalName).append("\n");
        }
        return sb.toString();
    }
}

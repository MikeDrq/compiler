package Mips.MipsInstruction;

import Mips.Register;

public class Addi extends MipsInstruction {
    private int reg1; //源
    private int reg2; //目标
    private int imme;

    public Addi(int reg1,int reg2,int imme) {
        super("addi");
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.imme = imme;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("addi ").append(register.getRegister(reg2)).append(", ").append(register.getRegister(reg1)).append(", ").append(imme).append("\n");
        return sb.toString();
    }
}

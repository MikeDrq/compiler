package Mips.MipsInstruction;

import Mips.Register;

public class Move extends MipsInstruction {
    private int des;
    private int src;

    public Move(int des,int src) {
        super("move");
        this.des = des;
        this.src = src;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("move ").append(register.getRegister(des)).append(", ").append(register.getRegister(src)).append("\n");
        return sb.toString();
    }
}

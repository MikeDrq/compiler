package Mips.MipsInstruction;

import Mips.Register;

public class Beq extends MipsInstruction{
    private int num;
    private String labelF;
    public Beq(int num, String labelF) {
        super("beq");
        this.num = num;
        this.labelF = labelF;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("beq ").append(register.getRegister(num)).append(", 0, label_").append(labelF).append("\n");
        return sb.toString();
    }
}

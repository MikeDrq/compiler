package Mips.MipsInstruction;

import Mips.Register;

public class La extends MipsInstruction{

    private int num;
    private String str;
    public La(int num,String str) {
        super("la");
        this.num = num;
        this.str = str;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("la ").append(register.getRegister(num)).append(", ").append(str).append("\n");
        return sb.toString();
    }
}

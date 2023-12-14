package Mips.MipsInstruction;

import Mips.Register;

public class Mflo extends MipsInstruction{
    private int reg;
    public Mflo(int reg) {
        super("mflo");
        this.reg = reg;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("mflo ").append(register.getRegister(reg)).append("\n");
        return sb.toString();
    }
}

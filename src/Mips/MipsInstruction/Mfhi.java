package Mips.MipsInstruction;

import Mips.Register;

public class Mfhi extends MipsInstruction{
    private int reg;
    public Mfhi(int reg) {
        super("mfhi");
        this.reg = reg;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("mfhi ").append(register.getRegister(reg)).append("\n");
        return sb.toString();
    }
}

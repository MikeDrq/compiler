package Mips.MipsInstruction;

import Mips.Register;

public class Jr extends MipsInstruction {
    private int reg;
    public Jr(int reg) {
        super("jr");
        this.reg = reg;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("jr ").append(register.getRegister(reg)).append("\n");
        return sb.toString();
    }
}

package Mips.MipsInstruction;

import Mips.Register;

public class Li extends MipsInstruction {
    private int regNum;
    private int value;

    public Li(int regNum,int value) {
        super("Li");
        this.regNum = regNum;
        this.value = value;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append("li ").append(register.getRegister(regNum)).append(", ").append(value).append("\n");
        return sb.toString();
    }
}

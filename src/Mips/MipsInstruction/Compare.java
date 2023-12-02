package Mips.MipsInstruction;

import Mips.Register;

public class Compare extends MipsInstruction {
    private String op;
    private int reg1;
    private int reg2;
    private int reg3;
    public Compare(String name,int reg1,int reg2,int reg3) {
        super(name);
        this.op = name;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.reg3 = reg3;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        sb.append(op).append(" ").append(register.getRegister(reg3)).append(", ")
                .append(register.getRegister(reg1)).append(", ").append(register.getRegister(reg2)).append("\n");
        return sb.toString();
    }
}

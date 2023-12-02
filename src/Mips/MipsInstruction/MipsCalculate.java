package Mips.MipsInstruction;

import Mips.Register;

public class MipsCalculate extends MipsInstruction{
    private String name;
    private String op;
    private int reg1;
    private int reg2;
    private int reg3;
    public MipsCalculate(String name,int reg1,int reg2,int reg3) {
        super(name);
        this.op = name;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.reg3 = reg3; //Op reg3,reg1,reg2
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        Register register = new Register();
        if (op.equals("addu") || op.equals("subu") || op.equals("mul") || op.equals("div") || op.equals("and") || op.equals("or")) {
            sb.append(op).append(" ").append(register.getRegister(reg3)).append(", ").append(register.getRegister(reg1))
                    .append(", ").append(register.getRegister(reg2)).append("\n");
        } else if (op.equals("srem")) {
            sb.append("div").append(" ").append(register.getRegister(reg1)).append(", ").append(register.getRegister(reg2))
                    .append("\n");
        } else {
            System.out.println("not icmp,not and,not or!");
        }
        return sb.toString();
    }
}

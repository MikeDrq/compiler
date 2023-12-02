package Mips.MipsInstruction;

public class Jal extends MipsInstruction {
    private String funcName;

    public Jal(String funcName) {
        super("jal");
        this.funcName = funcName;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("jal ").append(funcName).append("\n");
        return sb.toString();
    }
}

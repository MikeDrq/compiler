package Mips.MipsInstruction;

public class J extends MipsInstruction {
    private int label;
    public J(int num) {
        super("j");
        this.label = num;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("j ").append("label_").append(label).append("\n");
        return sb.toString();
    }
}

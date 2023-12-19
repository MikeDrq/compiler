package Mips.MipsInstruction;

public class J extends MipsInstruction {
    private String label;
    public J(String label) {
        super("j");
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("j label_").append(label).append("\n");
        return sb.toString();
    }
}

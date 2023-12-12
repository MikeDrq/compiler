package Mips.MipsInstruction;

public class MipsLabel extends MipsInstruction {
    private String name;
    public MipsLabel(String name) {
        super("label");
        this.name = name;
    }

    @Override
    public String mipsOutput() {
        return "label_" + name + ":\n";
    }
}

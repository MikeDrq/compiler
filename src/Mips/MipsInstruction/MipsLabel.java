package Mips.MipsInstruction;

public class MipsLabel extends MipsInstruction {
    private int  num;
    public MipsLabel(int num) {
        super("label");
        this.num = num;
    }

    @Override
    public String mipsOutput() {
        return "label_" + num + ":\n";
    }
}

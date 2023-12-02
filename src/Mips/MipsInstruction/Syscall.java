package Mips.MipsInstruction;

public class Syscall extends MipsInstruction{
    public Syscall() {
        super("syscall");
    }

    @Override
    public String mipsOutput() {
        return "syscall\n";
    }
}

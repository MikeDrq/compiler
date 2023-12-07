package Middle.Value.Instruction.AllInstructions;

import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Phi extends Instruction {
    private String firstLeft = "";
    private String firstRight = "";
    private String secondLeft = "";
    private String secondRight = "";
    public Phi(String name, ValueType valueType) {
        super(name,valueType);
    }

    @Override
    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName()).append("=").append("phi i32 [").append(firstLeft).append(",").append(firstRight).
                append("], [").append(secondLeft).append(",").append(secondRight).append("]\n");
        return sb.toString();
    }
}

package Middle.Value.Instruction.AllInstructions;

import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Label extends Instruction {

    public Label(String name, ValueType valueType) {
        super(name,valueType);
    }
    @Override
    public String midOutput() {
        return "label_" + super.getName() + ":\n";
    }
}

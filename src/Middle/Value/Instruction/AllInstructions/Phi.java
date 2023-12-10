package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIr;
import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Phi extends Instruction {
    private ArrayList<LlvmIrValue> values;
    private ArrayList<LlvmIrValue> source;
    private String changeName;

    public Phi(String name, ValueType valueType,String changeName) {
        super(name,valueType); //这个name用于替换
        values = new ArrayList<>();
        source = new ArrayList<>();
        this.changeName = changeName;
    }

    public void addValue(LlvmIrValue value,LlvmIrValue src) {
           values.add(value);
           source.add(src);
    }

    public ArrayList<LlvmIrValue> getSource() {
        return source;
    }

    public ArrayList<LlvmIrValue> getValues() {
        return values;
    }

    public String getChangeName() {
        return this.changeName;
    }

    @Override
    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName()).append("=").append("phi i32 ");
        for (int i = 0;i < values.size();i++) {
            sb.append("[ ").append(values.get(i).getName()).append(" , %label_").append(source.get(i).getName()).append(" ]");
            if (i != values.size()-1) {
                sb.append(", ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}

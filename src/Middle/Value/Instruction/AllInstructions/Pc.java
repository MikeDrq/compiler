package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Pc extends Instruction {
    String bbBelong;
    ArrayList<LlvmIrValue> values;
    ArrayList<LlvmIrValue> phis;
    public Pc (String name, ValueType valueType,String bbBelong) {
        super(name,valueType);
        this.bbBelong = bbBelong;
        this.values = new ArrayList<>();
        this.phis = new ArrayList<>();
    }

    public String getBbBelong() {
        return this.bbBelong;
    }

    public void addValue(Instruction phi,LlvmIrValue value) {
        this.phis.add(phi);
        this.values.add(value);
    }

    public ArrayList<LlvmIrValue> getPhis() {
        return phis;
    }

    public ArrayList<LlvmIrValue> getValues() {
        return values;
    }

    @Override
    public String midOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("pccccc");
        for (int i = 0;i < phis.size();i++) {
            sb.append(phis.get(i).getName()).append(" ").append(values.get(i).getName());
;        }
        sb.append("\n");
        return sb.toString();
    }
}

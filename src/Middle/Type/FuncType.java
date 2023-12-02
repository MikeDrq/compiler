package Middle.Type;

import Middle.LlvmIrValue;

import java.util.ArrayList;

public class FuncType extends ValueType {
    private ValueType valueType;
    private ArrayList<LlvmIrValue> params;

    public FuncType(ValueType valueType,ArrayList<LlvmIrValue> params) {
        this.valueType = valueType;
        this.params = params;
    }

    public ArrayList<LlvmIrValue> getParams() {
        return this.params;
    }

    public ValueType getRetType() {
        return valueType;
    }

    @Override
    public String midOutput() {
        if (valueType instanceof IntType) {
            return "i32";
        } else {
            return "void";
        }
    }
}

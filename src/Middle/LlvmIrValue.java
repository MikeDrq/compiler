package Middle;

import Lexer.Token;
import Middle.Type.ValueType;

import java.util.ArrayList;

public class LlvmIrValue {

    private ValueType type;

    private String name;

    private int dim = 0;

    private int rParamDim = -1; //函数传参判断时使用

    private int raw;

    private int column;


    private boolean isParam = false;

    public LlvmIrValue(String name, ValueType type) {
        this.name = name;
        this.type = type;
    }

    public ValueType getType() {
        return type;
    }

    public void setRParamDim(int rParamDim) {
        this.rParamDim = rParamDim;
    }

    public int getRParamDim() {
        return this.rParamDim;
    }

    public int getDim() {
        return this.dim;
    }

    public void setIsParam(boolean isParam) {
        this.isParam = isParam;
    }

    public String getName() {
        return name;
    }

    public int getColumn() {
        return this.column;
    }

    public int getRaw() {
        return raw;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setRaw(int raw) {
        this.raw = raw;
    }
}

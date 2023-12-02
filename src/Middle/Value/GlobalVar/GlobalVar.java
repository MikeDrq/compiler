package Middle.Value.GlobalVar;

import Middle.LlvmIrValue;
import Middle.Type.ValueType;
import Middle.Value.Constant.Constant;
import Middle.Value.Instruction.AllInstructions.Load;
import com.sun.jdi.Value;

import java.util.ArrayList;

public class GlobalVar extends LlvmIrValue {
    private int dim;
    private boolean isConst;
    private Constant constant;
    private ValueType type;

    public GlobalVar(String name, ValueType type, Constant constant, Boolean isConst, Integer dim) {
        super(name,type);
        this.constant = constant;
        this.isConst = isConst;
        this.dim = dim;
        this.type =type;
    }

    public int getDim() {
        return this.dim;
    }

    public int getValue() {
        return this.constant.getValue();
    }

    public ArrayList<Integer> getOneDimArray() {
        return constant.getOneDimArray();
    }

    public ArrayList<ArrayList<Integer>> getTwoDimArray() {
        return constant.getTwoDimArray();
    }

    public String midOutput() {
        String s = "";
        if (isConst) {
            if (dim == 0) {
                s = this.getName() + " = dso_local constant "
                        + this.type.midOutput()
                        + " " + this.constant.midOutput() + "\n";
            } else if (dim == 1) {
                s = this.getName() + " = dso_local constant " + this.type.midOutput() + this.constant.midOutput() + "\n";
            } else if (dim == 2) {
                s = this.getName() + " = dso_local constant " + this.type.midOutput() + this.constant.midOutput() + "\n";
            } else {
                System.out.println("error");
            }
        } else {
            if (dim == 0) {
                s = this.getName() + " = dso_local global "
                        + this.type.midOutput()
                        + " " + this.constant.midOutput() + "\n";
            } else if (dim == 1) {
                s = this.getName() + " = dso_local global " + this.type.midOutput() + this.constant.midOutput() + "\n";
            } else if (dim == 2) {
                s = this.getName() + " = dso_local global " + this.type.midOutput() + this.constant.midOutput() + "\n";
            } else {
                System.out.println("error");
            }
        }
        return s;
    }
}

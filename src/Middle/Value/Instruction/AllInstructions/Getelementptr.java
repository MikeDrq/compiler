package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Type.PointerType;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

import java.util.ArrayList;

public class Getelementptr extends Instruction {

    private LlvmIrValue base;
    private int dim;
    //private String offset_row;
    //private String offset_column;
    private LlvmIrValue offset_row_value;
    private LlvmIrValue offset_column_value;

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int column,int offset) { //初始化
        super(name,valueType);
        super.setDim(dim);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_column_value = new LlvmIrValue(String.valueOf(offset),new IntType(32));
    }

    public Getelementptr(String name, ValueType valueType, LlvmIrValue base, int dim, int column,LlvmIrValue offset) {
        super(name,valueType);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_column_value = offset;
        super.setDim(dim);
    }

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int row,int column,int offset_row,int offset_column) { //初始化
        super(name,valueType);
        super.setDim(dim);
        super.setRaw(row);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_row_value = new LlvmIrValue(String.valueOf(offset_row),new IntType(32));
        this.offset_column_value = new LlvmIrValue(String.valueOf(offset_column),new IntType(32));
    }

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int row,int column,LlvmIrValue offset_row,LlvmIrValue offset_column) {
        super(name,valueType);
        super.setDim(dim);
        super.setColumn(column);
        super.setRaw(row);
        this.base = base;
        this.dim = dim;
        this.offset_row_value = offset_row;
        this.offset_column_value = offset_column;
    }

    public int getDim() {
        return this.dim;
    }

    public LlvmIrValue getOffsetColumn() {
        return offset_column_value;
    }

    public LlvmIrValue getBase() {
        return base;
    }

    public LlvmIrValue getOffsetRow() {
        return offset_row_value;
    }

    public int getRowNum() {
        if (super.getType() instanceof  PointerType) {
            return ((PointerType) super.getType()).getRowNum();
        } else {
            return super.getRaw();
        }
    }

    public int getColumnNum() {
        if (super.getType() instanceof PointerType) {
            return ((PointerType) super.getType()).getColumnNum();
        } else {
            return super.getColumn();
        }
    }

    //<result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*
    @Override
    public String midOutput() {
        String s = "";
        /*if (dim == 1) {
            String part = "[" + column + " x i32]";
            s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                    + base.getName() + ",i32 0,i32 " + offset_column + "\n";
        } else if (dim == 2) {
            String part = "[" + row + " x [" + column +" x i32]]";
            s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                    + base.getName() + ",i32 0,i32 " + offset_row + ",i32 " + offset_column +  "\n";
        }*/
        String part; //只有一个 *
        if (super.getType() instanceof PointerType) {
            part = ((PointerType) super.getType()).specialForGetelement();
            if (dim == 1) {
                if (super.getRParamDim() == 1) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + "\n";
                } else {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + ",i32 " + offset_column_value.getName() + "\n";
                }
            } else if (dim == 2) {
                if (super.getRParamDim() == 2) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + "\n";
                } else if (super.getRParamDim() == 1) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + ",i32 " + offset_row_value.getName() + ",i32 0\n";
                } else {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 " + offset_row_value.getName() + ",i32 " + offset_column_value.getName() + "\n";
                }
            }
        } else {
            part = super.getType().midOutput();
            if (dim == 1) {
                s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                        + base.getName() + ",i32 0,i32 " + offset_column_value.getName() + "\n";
            } else if (dim == 2) {
                if (super.getRParamDim() == 2) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 0,i32 0\n";
                } else {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 0,i32 " + offset_row_value.getName() + ",i32 " + offset_column_value.getName() + "\n";
                }
            }
        }
        return s;
    }

    @Override
    public LlvmIrValue getDefine() {
        return this;
    }

    @Override
    public ArrayList<LlvmIrValue> getOperand() {
        ArrayList<LlvmIrValue> h = new ArrayList<>();
        h.add(base);
        if (offset_row_value != null) {
            h.add(offset_row_value);
        }
        if (offset_column_value != null) {
            h.add(offset_column_value);
        }
        return h;
    }

    @Override
    public void change(String name,LlvmIrValue l) {
        if (base.getName().equals(name)) {
            this.base = l;
        }
        if (offset_column_value != null) {
            if (offset_column_value.getName().equals(name)) {
                offset_column_value = l;
            }
        }
        if (offset_row_value != null) {
            if (offset_row_value.getName().equals(name)) {
                offset_row_value = l;
            }
        }
    }
}

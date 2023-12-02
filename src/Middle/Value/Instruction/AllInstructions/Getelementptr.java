package Middle.Value.Instruction.AllInstructions;

import Middle.LlvmIrValue;
import Middle.Type.PointerType;
import Middle.Type.ValueType;
import Middle.Value.Instruction.Instruction;

public class Getelementptr extends Instruction {

    private LlvmIrValue base;
    private int dim;
    private String offset_row;
    private String offset_column;
    private LlvmIrValue offset_row_value;
    private LlvmIrValue offset_column_value;

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int column,int offset) { //初始化
        super(name,valueType);
        super.setDim(dim);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_column = String.valueOf(offset);
    }

    public Getelementptr(String name, ValueType valueType, LlvmIrValue base, int dim, int column,LlvmIrValue offset) {
        super(name,valueType);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_column_value = offset;
        this.offset_column = offset.getName();
        super.setDim(dim);
    }

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int row,int column,int offset_row,int offset_column) { //初始化
        super(name,valueType);
        super.setDim(dim);
        super.setRaw(row);
        super.setColumn(column);
        this.base = base;
        this.dim = dim;
        this.offset_row = String.valueOf(offset_row);
        this.offset_column = String.valueOf(offset_column);
    }

    public Getelementptr(String name, ValueType valueType,LlvmIrValue base,int dim,int row,int column,LlvmIrValue offset_row,LlvmIrValue offset_column) {
        super(name,valueType);
        super.setDim(dim);
        super.setColumn(column);
        super.setRaw(row);
        this.base = base;
        this.dim = dim;
        this.offset_row = offset_row.getName();
        this.offset_column = offset_column.getName();
        this.offset_row_value = offset_row;
        this.offset_column_value = offset_column;
    }

    public int getDim() {
        return this.dim;
    }

    public String getOffsetColumn() {
        return offset_column;
    }

    public String getBaseName() {
        return base.getName();
    }

    public String getOffsetRow() {
        return offset_row;
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
                            + base.getName() + ",i32 " + offset_column + "\n";
                }
            } else if (dim == 2) {
                if (super.getRParamDim() == 2) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + "\n";
                } else if (super.getRParamDim() == 1) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                            + base.getName() + ",i32 " + offset_row + ",i32 0\n";
                } else {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 " + offset_row + ",i32 " + offset_column + "\n";
                }
            }
        } else {
            part = super.getType().midOutput();
            if (dim == 1) {
                s = super.getName() + " = getelementptr " + part + ", " + part + "* "
                        + base.getName() + ",i32 0,i32 " + offset_column + "\n";
            } else if (dim == 2) {
                if (super.getRParamDim() == 2) {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 0,i32 0\n";
                } else {
                    s = super.getName() + " = getelementptr " + part + ", " + part + "* " +
                            base.getName() + ",i32 0,i32 " + offset_row + ",i32 " + offset_column + "\n";
                }
            }
        }
        return s;
    }
}

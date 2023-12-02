package Middle.Type;

public class ArrayType extends ValueType {
    private int dim;

    private int row;

    private int column;

    public ArrayType(int dim,int row,int column) {
        this.dim = dim;
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getDim() {
        return dim;
    }

    public String getInner() {
        return "[" + column + " x i32]";
    }

    @Override
    public String midOutput() {
        if (dim == 1) {
            return "[" + column + " x i32]";
        } else {
            return "[" + row + " x [" + column + " x i32]]";
        }
    }
}

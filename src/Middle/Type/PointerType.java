package Middle.Type;

public class PointerType extends ValueType {
    private ValueType valueType;
    public PointerType(ValueType valueType) {
        this.valueType  = valueType;
    }

    public String specialForGetelement(){
        String s = "";
        if (valueType instanceof ArrayType) {
            ArrayType avt = (ArrayType) valueType;
            int dim = avt.getDim();
            int column = avt.getColumn();
            if (dim == 1) {
                return "i32";
            } else {
                return "[" + column + " x i32]";
            }
        } else {
            System.out.println("illegal type");
        }
        return s;
    }

    public int getRowNum() {
        if (valueType instanceof ArrayType) {
            ArrayType avt = (ArrayType) valueType;
            return avt.getRow();
        } else {
            System.out.println("illegal type");
            return -1;
        }
    }

    public int getColumnNum() {
        if (valueType instanceof ArrayType) {
            ArrayType avt = (ArrayType) valueType;
            return avt.getColumn();
        } else {
            System.out.println("illegal type");
            return -1;
        }
    }

    @Override
    public String midOutput() {
        String s = "";
        if (valueType instanceof IntType) {
            s = valueType.midOutput() + "*";
            System.out.println("do some check");
        } else if (valueType instanceof ArrayType) {
            ArrayType avt = (ArrayType) valueType;
            int dim = avt.getDim();
            int column = avt.getColumn();
            if (dim == 1) {
                return "i32*";
            } else {
                return "[" + column + " x i32]*";
            }
         } else {
            System.out.println("illegal type");
        }
        return s;
    }
}

package Middle.Value.Constant;

import Middle.Type.ValueType;

import java.util.ArrayList;

public class Constant {
    private int value;

    private ValueType type;

    private int row;

    private int column;

    private int dim = 0;

    private Boolean isConst;

    private ArrayList<Constant> oneDimArray;

    private ArrayList<ArrayList<Constant>> twoDimArray;

    private boolean oneIsFullZero;

    private boolean twoIsFullZero;

    private ArrayList<Boolean> twoIsPartialZero;


    public Constant (ValueType type, int value) {
        this.type = type;
        this.value = value;
    }

    public Constant (ValueType type,int dim,boolean isConst) {
        this.type = type;
        this.dim = dim;
        this.isConst = isConst;
    }

    public void setOneIsFullZero(boolean flag) {
        this.oneIsFullZero = flag;
    }

    public void setOneDimArray(ArrayList<Constant> ar) {
        this.oneDimArray = ar;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public ValueType getType() {
        return type;
    }

    public Integer getValue() {
        return this.value;
    }

    public ArrayList<Integer> getOneDimArray() {
        ArrayList<Integer> ar = new ArrayList<>();
        if (oneIsFullZero) {
            for (int i = 0;i < column;i++) {
                ar.add(0);
            }
        } else {
            for (int i = 0;i < column;i++) {
                if (i < oneDimArray.size()) {
                    ar.add(oneDimArray.get(i).getValue());
                } else {
                    ar.add(0);
                }
            }
        }
        return ar;
    }

    public ArrayList<ArrayList<Integer>> getTwoDimArray() {
        ArrayList<ArrayList<Integer>> arr = new ArrayList<>();
        if (twoIsFullZero) {
            for (int i = 0;i < row;i++) {
                ArrayList<Integer> ar = new ArrayList<>();
                for(int j = 0;j < column;j++) {
                    ar.add(0);
                }
                arr.add(ar);
            }
        } else {
            for (int i = 0;i < row;i++) {
                if (i < twoDimArray.size()) {
                    if (twoIsPartialZero.get(i)) {
                        ArrayList<Integer> ar = new ArrayList<>();
                        for (int j = 0;j < column;j++) {
                            ar.add(0);
                        }
                        arr.add(ar);
                    } else {
                        ArrayList<Constant>  temp = twoDimArray.get(i);
                        ArrayList<Integer> ar = new ArrayList<>();
                        for (int j = 0;j < column;j++) {
                            if (j < temp.size()) {
                                ar.add(temp.get(j).getValue());
                            } else {
                                ar.add(0);
                            }
                        }
                        arr.add(ar);
                    }
                } else {
                    ArrayList<Integer> ar = new ArrayList<>();
                    for (int j = 0;j < column;j++) {
                        ar.add(0);
                    }
                    arr.add(ar);
                }
            }
        }
        return arr;
    }

    public void setTwoDimArray(ArrayList<ArrayList<Constant>> ar) {
        this.twoDimArray = ar;
    }

    public void setTwoIsPartialZero(ArrayList<Boolean> b) {
        this.twoIsPartialZero = b;
    }

    public void setTwoIsFullZero(Boolean b) {
        this.twoIsFullZero = b;
    }

    public String midOutput() {
        if (dim == 0) {
            return String.valueOf(value);
        } else {
            if (isConst) {
                if (dim == 1) {
                    String s = "";
                    if (oneIsFullZero) {
                        s = s + "zeroinitializer";
                    } else {
                        s = s + outPutOneArray(oneDimArray);
                    }
                    return s;
                } else if (dim == 2) {
                    StringBuilder s = new StringBuilder();
                    if (twoIsFullZero) {
                        s.append("zeroinitializer");
                    } else {
                        s.append("[");
                        for (int i = 0;i < row;i++) {
                            s.append("[").append(column).append(" x ").append("i32]");
                            if (twoIsPartialZero.get(i)) {
                                s.append("zeroinitializer");
                            } else {
                                ArrayList<Constant> temp = twoDimArray.get(i);
                                s.append(outPutOneArray(temp));
                            }
                            if (i != twoDimArray.size() - 1) {
                                s.append(",");
                            }
                        }
                        s.append("]");
                    }
                    return s.toString();
                }
            } else {
                if (dim == 1) {
                    String s = "";
                    if (oneIsFullZero) {
                        s = s + "zeroinitializer";
                    } else {
                        s = s + outputVarArray(oneDimArray);
                    }
                    return s;
                } else {
                    StringBuilder s = new StringBuilder();
                    if (twoIsFullZero) {
                        s.append("zeroinitializer");
                    } else {
                        s.append("[");
                        for (int i = 0;i < row;i++) {
                            s.append("[").append(column).append(" x i32] ");
                            if (i < twoDimArray.size()) {
                                if (twoIsPartialZero.get(i)) {
                                    s.append("zeroinitializer");
                                } else {
                                    s.append(outputVarArray(twoDimArray.get(i)));
                                }
                            } else {
                                s.append("zeroinitializer");
                            }
                            if (i != row - 1) {
                                s.append(",");
                            }
                        }
                        s.append("]");
                    }
                    return s.toString();
                }
            }
        }
        return "";
    }

    private String outPutOneArray(ArrayList<Constant> ar) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < ar.size(); i++) {
            s.append(ar.get(i).getType().midOutput()).append(" ").append(ar.get(i).getValue());
            if (i != ar.size() - 1) {
                s.append(",");
            }
        }
        s.append("]");
        return s.toString();
    }

    private String outputVarArray(ArrayList<Constant> ar) {
        StringBuilder s = new StringBuilder();
        for (int i = 0;i < column;i++) {
            if (i < ar.size()) {
                s.append(outPutOneArray(ar));
                i = ar.size() - 1;
                s.deleteCharAt(s.length() - 1);
            } else {
                s.append(",").append("i32").append(" 0");
            }
        }
        s.append("]");
        return s.toString();
    }
}

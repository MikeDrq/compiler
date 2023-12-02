package Mips.MipsGlobalVar;

import Mips.MipsValue;

import java.util.ArrayList;

public class MipsGlobalVar implements MipsValue {
    private int dim;
    private String name;
    private int value;
    private ArrayList<Integer> oneDimArray;
    private ArrayList<ArrayList<Integer>> twoDimArray;

    public MipsGlobalVar(int dim,String name,int value) {
        this.dim = dim;
        this.name = name;
        this.value = value;
    }

    public MipsGlobalVar(int dim, String name, ArrayList<Integer> oneDimArray) {
        this.dim = dim;
        this.name = name;
        this.oneDimArray = oneDimArray;
    }

    public MipsGlobalVar(int dim, String name, ArrayList<ArrayList<Integer>>  twoDimArray,int flag) {
        this.dim = dim;
        this.name = name;
        this.twoDimArray = twoDimArray;
    }

    @Override
    public String mipsOutput() {
        StringBuilder sb = new StringBuilder();
        if (dim == 0) {
            sb.append(name.substring(1)).append(": .word ").append(value).append("\n");
        } else if (dim == 1) {
            sb.append(name.substring(1)).append(": .word ");
            for (int i = 0;i < oneDimArray.size();i++) {
                int num = oneDimArray.get(i);
                sb.append(num);
                if (i != oneDimArray.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n");
        } else if (dim == 2) {
            sb.append(name.substring(1)).append(": .word ");
            for (int i = 0;i < twoDimArray.size();i++) {
                ArrayList<Integer> temp = twoDimArray.get(i);
                for (int j = 0;j <temp.size();j++) {
                    int num = temp.get(j);
                    sb.append(num);
                    if (j != temp.size() - 1) {
                        sb.append(", ");
                    }
                }
                if (i != twoDimArray.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}

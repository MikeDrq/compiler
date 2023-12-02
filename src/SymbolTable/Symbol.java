package SymbolTable;

import Lexer.Token;
import Middle.LlvmIrValue;

import java.util.ArrayList;


public class Symbol {
    private int line; //所在行
    private String token; //名
    private SymbolType type; //种类
    //array
    private int dim; //数组维数
    private int array_row; //列数
    private int array_column;  //二维的行数
    private Boolean allZero = false; //判断数组是否全是 0,全 0 则不额外初始化。
    //func
    private int retype; //0 -> void,1 -> int
    private int paramNumber;
    private ArrayList<Symbol> paramList;

    //initial
    private int initial;
    private ArrayList<Integer> oneDimInit;
    private ArrayList<ArrayList<Integer>> twoDimInit;

    private LlvmIrValue llvmIrValue; //生成llvm ir时使用

    public Symbol(Token token, SymbolType type) {
        this.line = token.getLine();
        this.token = token.getToken();
        this.type = type;
    }

    public void addDimension(Integer dim) {
        this.dim = dim;
    }

    public String getName() {
        return this.token;
    }

    public void setIsVoid(Boolean isVoid) {
        if (isVoid) {
            this.retype = 0;
        } else {
            this.retype = 1;
        }
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public void setLRNumber(int row,int column) {
        this.array_row = row;
        this.array_column = column;
    }

    public int getArray_row() {
        return array_row;
    }

    public int getArray_column() {
        return array_column;
    }

    public int getDim() {
        return this.dim;
    }

    public LlvmIrValue getLlvmIrValue() {
        return llvmIrValue;
    }

    public int getNumValue() {
        return initial;
    }

    public void setInitial(int num) {
        this.initial = num;
    }

    public SymbolType getType() {
        return this.type;
    }

    public void setParams(ArrayList<Symbol> symbols) {
        this.paramList = symbols;
        this.paramNumber = paramList.size();
    }

    public ArrayList<Symbol> getParams() {
        return this.paramList;
    }

    public int getLine() {
        return this.line;
    }

    public int getRetype() {
        return retype;
    }

    public int getInit() {
        return initial;
    }

    public void setLlvmIrValue(LlvmIrValue llvmIrValue) {
        this.llvmIrValue = llvmIrValue;
    }

    public void setArrayOneInitial(ArrayList<Integer> ar) {
        this.oneDimInit = ar;
    }

    public void setArrayTwoInitial(ArrayList<ArrayList<Integer>> ar) {
        this.twoDimInit = ar;
    }

    public ArrayList<Integer> getOneDimInit() {
        return oneDimInit;
    }

    public ArrayList<ArrayList<Integer>> getTwoDimInit() {
        return twoDimInit;
    }

    public void setAllZero(Boolean b) {
        this.allZero = b;
    }

    public Boolean getAllZero() {
        return this.allZero;
    }

    public ArrayList<Symbol> getParamList() {
        return paramList;
    }
}


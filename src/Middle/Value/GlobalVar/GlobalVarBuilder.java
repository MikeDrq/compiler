package Middle.Value.GlobalVar;

import Lexer.Token;
import Middle.Type.ArrayType;
import Middle.Type.IntType;
import Middle.Type.ValueType;
import Middle.Value.Constant.Constant;
import SymbolTable.SymbolTable;
import SyntaxTree.*;
import SymbolTable.Symbol;
import SymbolTable.SymbolType;
import java.util.ArrayList;

public class GlobalVarBuilder {
    private DeclNode declNode;
    private SymbolTable symbolTable;

    public GlobalVarBuilder(DeclNode declNode,SymbolTable symbolTable) {
        this.declNode = declNode;
        this.symbolTable = symbolTable;
    }

    public ArrayList<GlobalVar> generateGlobalVar() {
        ArrayList<GlobalVar> globalVars = new ArrayList<>();
        if (declNode.queryConst()) {
            ConstDeclNode constDeclNode = declNode.getConstDeclNode();
            ArrayList<ConstDefNode> constDefNodes = constDeclNode.getConstDefNodes();
            for (ConstDefNode constDefNode:constDefNodes) {
                globalVars.add(generateGlobalConstVar(constDefNode));
            }
        } else {
            VarDeclNode varDeclNode = declNode.getVarDeclNode();
            ArrayList<VarDefNode> varDefNodes = varDeclNode.getVarDefNodes();
            for (VarDefNode varDefNode : varDefNodes) {
                globalVars.add(generateGlobalVarVar(varDefNode));
            }
        }
        return globalVars;
    }

    public GlobalVar generateGlobalConstVar(ConstDefNode constDefNode) {
        Token ident = constDefNode.getIdent();
        int dim;
        int row = 0,column = 0; //数组的行、列
        SymbolType symbolType;
        if (constDefNode.getLBrackNum() == 0) {
            dim = 0;
            symbolType = SymbolType.CONST;
        } else if (constDefNode.getLBrackNum() == 1) {
            dim = 1;
            symbolType = SymbolType.CONST_ARRAY1;
            ArrayList<ConstExpNode> constExpNodes = constDefNode.getConstExpNodes();
            if (constExpNodes.size() != 1) {
                System.out.println("dim_error");
            }
            column = constExpNodes.get(0).calcuateValue(symbolTable);
        } else if (constDefNode.getLBrackNum() == 2) {
            dim = 2;
            symbolType = SymbolType.CONST_ARRAY2;
            ArrayList<ConstExpNode> constExpNodes = constDefNode.getConstExpNodes();
            if (constExpNodes.size() != 2) {
                System.out.println("dim_error");
            }
            row = constExpNodes.get(0).calcuateValue(symbolTable);
            column = constExpNodes.get(1).calcuateValue(symbolTable);
        } else {
            dim = -1;
            symbolType = null;
            System.out.println("error");
        }
        Symbol symbol = new Symbol(ident,symbolType);
        symbol.setDim(dim);
        symbol.setLRNumber(row,column);
        symbolTable.addItem(symbol);
        setConInit(symbol,constDefNode.getConstInitValNode(),dim);
        GlobalVar globalVar = null;
        if (dim == 0) {
            ValueType intType = new IntType(32);
            Constant constant = new Constant(intType,symbol.getInit());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,intType,constant,true,0);
            symbol.setLlvmIrValue(globalVar);
        } else if (dim == 1) {
            //code_generation_2
            ValueType intType = new IntType(32);
            ArrayList<Integer> ar = symbol.getOneDimInit();
            int flag = 1;
            Constant ca = new Constant(new ArrayType(1,0,column),1,true);
            ArrayList<Constant> tc = new ArrayList<>();
            for (Integer i : ar) {
                tc.add(new Constant(intType,i));
                if (i != 0) {
                    flag = 0;
                }
            }
            ca.setOneDimArray(tc);
            if (flag == 1) {
                ca.setOneIsFullZero(true);
            } else {
                ca.setOneIsFullZero(false);
            }
            ca.setColumn(symbol.getArray_column());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,ca.getType(),ca,true,1);
            symbol.setLlvmIrValue(globalVar);
        } else if (dim == 2) {
            //code_generation_2
            ValueType intType = new IntType(32);
            ArrayList<ArrayList<Integer>> ar = symbol.getTwoDimInit();
            Constant ca = new Constant(new ArrayType(2,row,column),2,true);
            ca.setTwoIsFullZero(true);
            ArrayList<Boolean> pz = new ArrayList<>();
            ArrayList<ArrayList<Constant>> tc = new ArrayList<>();
            for (ArrayList<Integer> outar : ar) {
                ArrayList<Constant> inArry = new ArrayList<>();
                int flag = 1;
                for (Integer num : outar) {
                    Constant constant = new Constant(intType,num);
                    inArry.add(constant);
                    if (num != 0) {
                        flag = 0;
                        ca.setTwoIsFullZero(false);
                    }
                }
                tc.add(inArry);
                if (flag == 1) {
                    pz.add(true);
                } else {
                    pz.add(false);
                }
            }
            ca.setTwoDimArray(tc);
            ca.setTwoIsPartialZero(pz);
            ca.setColumn(symbol.getArray_column());
            ca.setRow(symbol.getArray_row());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,ca.getType(),ca,true,2);
            symbol.setLlvmIrValue(globalVar);
        } else {
            System.out.println("error");
        }
        return globalVar;
    }

    public GlobalVar generateGlobalVarVar(VarDefNode varDefNode) {
        Token ident = varDefNode.getIdent();
        int dim;
        SymbolType symbolType;
        int row = 0;
        int column = 0;
        if (varDefNode.getLBrackNum() == 0) {
            dim = 0;
            symbolType = SymbolType.VAR;
        } else if (varDefNode.getLBrackNum() == 1) {
            dim = 1;
            symbolType = SymbolType.VAR_ARRAY1;
            ArrayList<ConstExpNode> constExpNodes = varDefNode.getConstExpNodes();
            if (constExpNodes.size() != 1) {
                System.out.println("dim_error");
            }
            column = constExpNodes.get(0).calcuateValue(symbolTable);
        } else if (varDefNode.getLBrackNum() == 2) {
            dim = 2;
            symbolType = SymbolType.VAR_ARRAY2;
            ArrayList<ConstExpNode> constExpNodes = varDefNode.getConstExpNodes();
            if (constExpNodes.size() != 2) {
                System.out.println("dim_error");
            }
            row = constExpNodes.get(0).calcuateValue(symbolTable);
            column = constExpNodes.get(1).calcuateValue(symbolTable);
        } else {
            dim = -1;
            symbolType = null;
            System.out.println("error");
        }
        Symbol symbol = new Symbol(ident,symbolType);
        symbol.setDim(dim);
        symbol.setLRNumber(row,column);
        symbolTable.addItem(symbol);
        setVarInit(symbol,varDefNode.getInitValNode(),dim);
        GlobalVar globalVar = null;
        if (dim == 0) {
            ValueType intType = new IntType(32);
            Constant constant = new Constant(intType,symbol.getInit());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,intType,constant,false,0);
            symbol.setLlvmIrValue(globalVar);
        } else if (dim == 1) {
            //code_generation_2
            ValueType intType = new IntType(32);
            Constant constant_array = new Constant(new ArrayType(1,0,column),1,false);
            if (symbol.getAllZero()) {
                constant_array.setOneIsFullZero(true);
            } else {
                int flag = 1;
                ArrayList<Constant> tc = new ArrayList<>();
                for (int i = 0;i < symbol.getOneDimInit().size();i++) { //size -> column间的东西全0\
                    int num = symbol.getOneDimInit().get(i);
                    if(num != 0) {
                        flag = 0;
                    }
                    tc.add(new Constant(intType,num));
                }
                if (flag == 1) {
                    constant_array.setOneIsFullZero(true);
                } else {
                    constant_array.setOneIsFullZero(false);
                    constant_array.setOneDimArray(tc);
                }
            }
            constant_array.setColumn(symbol.getArray_column());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,constant_array.getType(),constant_array,false,1);
            symbol.setLlvmIrValue(globalVar);
        } else if (dim == 2) {
            //code_generation_2
            ValueType intType = new IntType(32);
            Constant constant_array = new Constant(new ArrayType(2,row,column),2,false);
            if (symbol.getAllZero()) {
                constant_array.setTwoIsFullZero(true);
            } else {
                constant_array.setTwoIsFullZero(true);
                ArrayList<Boolean> pz = new ArrayList<>();
                ArrayList<ArrayList<Constant>> tc = new ArrayList<>();
                for (int i = 0;i < symbol.getTwoDimInit().size();i++) {
                    ArrayList<Constant> arc = new ArrayList<>();
                    ArrayList<Integer> ari = symbol.getTwoDimInit().get(i);
                    int flag  = 1; //判断某一行是否全为0
                    for (int j = 0;j < ari.size();j++) {
                        int num = ari.get(j);
                        if (num != 0) {
                            flag = 0;
                            constant_array.setTwoIsFullZero(false);
                        }
                        arc.add(new Constant(intType,num));
                    }
                    if (flag == 1) {
                        pz.add(true);
                    } else {
                        pz.add(false);
                    }
                    tc.add(arc);
                }
                constant_array.setTwoDimArray(tc);
                constant_array.setTwoIsPartialZero(pz);
            }
            constant_array.setRow(symbol.getArray_row());
            constant_array.setColumn(symbol.getArray_column());
            String name = "@" + symbol.getName();
            globalVar = new GlobalVar(name,constant_array.getType(),constant_array,false,2);
            symbol.setLlvmIrValue(globalVar);
        } else {
            System.out.println("error");
        }
        return globalVar;
    }

    public void setConInit(Symbol symbol, ConstInitValNode constInitValNode,int dim) {
        if (dim == 0) {
            ConstExpNode constExpNode = constInitValNode.getConstExpNode();
            int num = constExpNode.calcuateValue(symbolTable);
            symbol.setInitial(num);
        } else if (dim == 1) {
            //code_generation_2
            int column =symbol.getArray_column();
            ArrayList<Integer> nums = new ArrayList<>();
            ArrayList<ConstInitValNode> constInitValNodes = constInitValNode.getConstInitValNodes();
            for (ConstInitValNode civ : constInitValNodes) {
                int num = civ.getConstExpNode().calcuateValue(symbolTable);
                nums.add(num);
            }
            symbol.setArrayOneInitial(nums);
        } else if (dim == 2) {
            //code_generation_2
            int row = symbol.getArray_row();
            int column = symbol.getArray_column();
            ArrayList<ArrayList<Integer>> nums = new ArrayList<>();
            ArrayList<ConstInitValNode> constInitValNodes = constInitValNode.getConstInitValNodes();
            for (ConstInitValNode civ : constInitValNodes) {
                ArrayList<ConstInitValNode> innerCiv = civ.getConstInitValNodes();
                ArrayList<Integer> inums = new ArrayList<>();
                for (ConstInitValNode iciv : innerCiv) {
                    int num = iciv.getConstExpNode().calcuateValue(symbolTable);
                    inums.add(num);
                }
                nums.add(inums);
            }
            symbol.setArrayTwoInitial(nums);
        } else {
            System.out.println("error");
        }
    }

    public void setVarInit(Symbol symbol, InitValNode initValNode,int dim) {
        if (dim == 0) {
            if (initValNode == null) {
                symbol.setInitial(0);
            } else {
                ExpNode expNode = initValNode.getExpNode();
                int num = expNode.calculateValue(symbolTable);
                symbol.setInitial(num);
            }
        } else if (dim == 1) {
            //code_generation_2
            ArrayList<Integer> ar = new ArrayList<>();
            if (initValNode == null) {
                symbol.setAllZero(true);
            } else {
                ArrayList<InitValNode> ivn = initValNode.getInitValNodes();
                for (int i = 0;i < ivn.size();i++) { //如果a[10] = {1,2,3} ，则符号表填的值只有1，2，3
                    int num = ivn.get(i).getExpNode().calculateValue(symbolTable);
                    ar.add(num);
                }
                symbol.setAllZero(false);
            }
            symbol.setArrayOneInitial(ar);
        } else if (dim == 2) {
            //code_generation_2
            ArrayList<ArrayList<Integer>> arr = new ArrayList<>();
            if (initValNode == null) {
                symbol.setAllZero(true);
            } else {
                ArrayList<InitValNode> ivn = initValNode.getInitValNodes();
                for (int i = 0;i < ivn.size();i++) {
                    ArrayList<Integer> ar = new ArrayList<>();
                    ArrayList<InitValNode> innerIvn = ivn.get(i).getInitValNodes();
                    for (int j = 0;j < innerIvn.size();j++) {
                        int num = innerIvn.get(j).getExpNode().calculateValue(symbolTable);
                        ar.add(num);
                    }
                    arr.add(ar);
                }
            }
            symbol.setArrayTwoInitial(arr);
        } else {
            System.out.println("error");
        }
    }
}

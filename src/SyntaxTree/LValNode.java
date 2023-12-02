package SyntaxTree;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;
import SymbolTable.Symbol;

import java.util.ArrayList;

public class LValNode extends TreeNode {
    private String name = "<LVal>";
    private Token ident;
    private ArrayList<Token> lbracks;
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> rbracks;

    public LValNode() {
        expNodes = new ArrayList<>();
        lbracks = new ArrayList<>();
        rbracks = new ArrayList<>();
    }

    public void addIdent(Token ident) {
        this.ident = ident;
    }

    public void addLbrack(Token lbrack) {
        lbracks.add(lbrack);
    }

    public void addRbrack(Token rbrack) {
        rbracks.add(rbrack);
    }

    public void addExpNode(ExpNode expNode) {
        expNodes.add(expNode);
    }

    public Token getIdent() {
        return this.ident;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return this.expNodes;
    }

    public int calculateValue(SymbolTable symbolTable) {
        Symbol symbol = symbolTable.getVar(ident);
        int dim = -1;
        if (symbol != null) {
            dim = symbol.getDim();
        } else {
            System.out.println("error");
        }
        if (dim == 0) {
            return symbol.getNumValue();
        } else if (dim == 1) {
            int pos = expNodes.get(0).calculateValue(symbolTable);
            if (symbol.getType() == SymbolType.VAR_ARRAY1) {
                if (pos >= symbol.getOneDimInit().size()) {
                    return 0;
                } else {
                    return symbol.getOneDimInit().get(pos);
                }
            } else if (symbol.getType() == SymbolType.CONST_ARRAY1) {
                return symbol.getOneDimInit().get(pos);
            } else {
                System.out.println("error");
            }
            //code_generation_2
        } else if (dim == 2) {
            int row = expNodes.get(0).calculateValue(symbolTable);
            int column = expNodes.get(1).calculateValue(symbolTable);
            if (symbol.getType() == SymbolType.VAR_ARRAY2) {
                if (row >= symbol.getTwoDimInit().size()) {
                    return 0;
                } else {
                    ArrayList<Integer> in = symbol.getTwoDimInit().get(row);
                    if (column >= in.size()) {
                        return 0;
                    } else {
                        return in.get(column);
                    }
                }
            } else if (symbol.getType() == SymbolType.CONST_ARRAY2) {
                return symbol.getTwoDimInit().get(row).get(column);
            }
            //code_generation_2
        } else {
            System.out.println("error");
        }
        return 0;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (lbracks.size() > 0) {
            for (int i = 0;i < lbracks.size();i++) {
                sb.append(lbracks.get(i).toString());
                sb.append(expNodes.get(i).print());
                sb.append(rbracks.get(i).toString());
            }
        }
        sb.append(name).append("\n");
        return sb.toString();
    }

    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        if (symbolTable.hasVar(ident) == null) {
            return null;
        }
        if (lbracks.size() == 2) {
            return SymbolType.VAR;
        } else if (lbracks.size() == 1) {
            Symbol symbol = symbolTable.hasVar(ident);
            if (symbol != null) {
                if (symbol.getType() == SymbolType.VAR_ARRAY1 || symbol.getType() == SymbolType.CONST_ARRAY1) {
                    return SymbolType.VAR;
                } else if (symbol.getType() == SymbolType.VAR_ARRAY2 || symbol.getType() == SymbolType.CONST_ARRAY2) {
                    return SymbolType.VAR_ARRAY1;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            Symbol symbol = symbolTable.hasVar(ident);
            if (symbol != null) {
                if (symbol.getType() == SymbolType.VAR_ARRAY1 || symbol.getType() == SymbolType.CONST_ARRAY1) {
                    return SymbolType.VAR_ARRAY1;
                } else if (symbol.getType() == SymbolType.VAR_ARRAY2 || symbol.getType() == SymbolType.CONST_ARRAY2) {
                    return SymbolType.VAR_ARRAY2;
                } else if (symbol.getType() == SymbolType.VAR || symbol.getType() == SymbolType.CONST) {
                    return SymbolType.VAR;
                }
                return symbol.getType();
            }
            return null; //未定义
        }
    }
}

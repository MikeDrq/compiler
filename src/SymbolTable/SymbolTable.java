package SymbolTable;

import Lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;

import Error.Error;
import Error.ErrorTable;
import SyntaxTree.ExpNode;
import SyntaxTree.FuncRParamsNode;

public class SymbolTable {
    private HashMap<String,Symbol> items;
    private ArrayList<SymbolTable> children;
    private SymbolTable parent;
    private int depth;

    public SymbolTable(SymbolTable parent) {
        items = new HashMap<>();
        children = new ArrayList<>();
        this.parent = parent;
        this.depth = 0;
    }

    public void addItem(Symbol symbol) {
       items.put(symbol.getName(),symbol);
    }

    public SymbolTable getParent() {
        return this.parent;
    }

    public void addChild(SymbolTable symbolTable) {
        this.children.add(symbolTable);
    }

    public boolean checkBTypeError(Symbol symbol) {
        return items.containsKey(symbol.getName());
    }

    public boolean checkCTypeError(Token token,int mark) {
        if (mark == 0) {
            if (this.items.containsKey(token.getToken())) {
                if (items.get(token.getToken()).getType() != SymbolType.FUNC) {
                    return true;
                } else {
                    return false;
                }
            } else if (parent != null) {
                return parent.checkCTypeError(token,0);
            } else {
                return false;
            }
        } else {
            if (this.items.containsKey(token.getToken())) {
                if (items.get(token.getToken()).getType() == SymbolType.FUNC) {
                    return true;
                } else {
                    return false;
                }
            } else if (parent != null) {
                return parent.checkCTypeError(token,1);
            } else {
                return false;
            }
        }
    }

    public ArrayList<Symbol> getFParamList(Token token) {
        if (items.containsKey(token.getToken())) {
            return items.get(token.getToken()).getParams();
        } else if (parent != null) {
            return parent.getFParamList(token);
        } else {
            return null;
        }
    }

    public void checkFuncRParam(ArrayList<Symbol> paramsList, int rParam, int funcLine, ErrorTable errorTable,
                                FuncRParamsNode funcRParamsNode,SymbolTable symbolTable) {
        if (paramsList != null) { //当函数定义无问题，再继续
            if (funcRParamsNode == null) {
                if (paramsList.size() != 0) {
                    errorTable.addError(new Error(funcLine,"d"));
                }
                return ;
            }
            ArrayList<ExpNode> expNodes = funcRParamsNode.getExpNodes();
            for (ExpNode expNode : expNodes) {
                if (expNode.getSymbolType(symbolTable) == null) {
                    return;
                }
            }
            if (paramsList.size() != rParam) {
                errorTable.addError(new Error(funcLine, "d"));
            } else {
                for (int i = 0;i < expNodes.size();i++) {
                    SymbolType symbolType = expNodes.get(i).getSymbolType(symbolTable);
                    if (symbolType != null) {
                        if (symbolType != paramsList.get(i).getType()) {
                            errorTable.addError(new Error(funcLine,"e"));
                            break;
                        }
                    }
                }
            }
        }
    }

    public Symbol getFunc(Token token) {
        if (this.items.containsKey(token.getToken())) {
            if (items.get(token.getToken()).getType() == SymbolType.FUNC) {
                return items.get(token.getToken());
            } else {
                return null;
            }
        } else if (parent != null) {
            return parent.getFunc(token);
        } else {
            return null;
        }
    }

    public Symbol hasVar(Token token) {
        if (this.items.containsKey(token.getToken())) {
            return items.get(token.getToken());
        } else if (parent != null) {
            return parent.hasVar(token);
        }
        return null;
    }

    public Symbol getVar(Token token) { //函数加变量
        if (this.items.containsKey(token.getToken())) {
            return items.get(token.getToken());
        } else if (parent != null) {
            return parent.hasVar(token);
        }
        return null;
    }

    public Symbol getVarByName(String name) {
        if (this.items.containsKey(name)) {
            return items.get(name);
        } else if (parent != null) {
            return parent.getVarByName(name);
        }
        return null;
    }
}

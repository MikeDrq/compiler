package SyntaxTree;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;

import java.util.ArrayList;

public class UnaryExpNode extends TreeNode{
    private String name = "<UnaryExp>";

    private PrimaryExpNode primaryExpNode;
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;
    private Token ident;
    private Token lparent;
    private FuncRParamsNode funcRParamsNodes;
    private Token rparent;

    public void addPrimaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }

    public void addUnaryOpNode(UnaryOpNode unaryOpNode) {
        this.unaryOpNode = unaryOpNode;
    }

    public void addUnaryExpNode(UnaryExpNode unaryExpNode) {
        this.unaryExpNode = unaryExpNode;
    }

    public void addIdent(Token ident) {
        this.ident = ident;
    }

    public void addLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void addFuncFParamsNodes(FuncRParamsNode funcRParamsNode) {
        this.funcRParamsNodes = funcRParamsNode;
    }

    public void addRparent(Token rparent) {
        this.rparent = rparent;
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public Token getIdent() {
        return ident;
    }

    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }

    public FuncRParamsNode getFuncRParamsNodes() {
        return funcRParamsNodes;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }
    public int calculateValue(SymbolTable symbolTable) {
        int ans = 0;
        if (primaryExpNode != null) {
            ans = primaryExpNode.calculateValue(symbolTable);
        } else if (ident != null) {
            System.out.println("error"); //global变量不会出现函数调用
        } else {
            if (unaryOpNode.getUnaryOpNode().getTokenType() == TokenType.PLUS) {
                ans = ans + unaryExpNode.calculateValue(symbolTable);
            } else if (unaryOpNode.getUnaryOpNode().getTokenType() == TokenType.MINU) {
                ans = ans - unaryExpNode.calculateValue(symbolTable);
            } else {
                System.out.println("error"); //!只出现在条件表达式
            }
        }
        return ans;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (primaryExpNode != null) {
            sb.append(primaryExpNode.print());
        } else if (ident != null){
            sb.append(ident.toString());
            sb.append(lparent.toString());
            if (funcRParamsNodes != null) {
                sb.append(funcRParamsNodes.print());
            }
            sb.append(rparent.toString());
        } else {
            sb.append(unaryOpNode.print());
            sb.append(unaryExpNode.print());
        }
        sb.append(name).append("\n");
        return sb.toString();
    }

    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        if (primaryExpNode != null) {
            return primaryExpNode.getSymbolType(symbolTable);
        } else if (ident != null){
            if (symbolTable.getFunc(ident) == null) {
                return null;
            }
            if (funcRParamsNodes == null) {
                return SymbolType.VAR;
            }
            ArrayList<ExpNode> expNodes = funcRParamsNodes.getExpNodes();
            for (ExpNode expNode : expNodes) {
                if (expNode.getSymbolType(symbolTable) == null) {
                    return null;
                }
            }
            if (symbolTable.getFunc(ident).getRetype() == 0) {
                return SymbolType.VOID;
            }
            return SymbolType.VAR;
        } else {
            return unaryExpNode.getSymbolType(symbolTable);
        }
    }
}

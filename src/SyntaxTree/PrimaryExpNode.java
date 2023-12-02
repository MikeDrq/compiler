package SyntaxTree;

import Lexer.Token;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;

import java.util.ArrayList;

public class PrimaryExpNode extends TreeNode{
    private String name = "<PrimaryExp>";
    private Token lparent;
    private ExpNode expNode;
    private Token rparent;
    private LValNode lValNode;
    private NumberNode numberNode;

    public void addLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void addRparent(Token rparent) {
        this.rparent = rparent;
    }


    public void addExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public void addlValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void addNumberNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }

    public int calculateValue(SymbolTable symbolTable) {
        if (lValNode != null) {
            return lValNode.calculateValue(symbolTable);
        } else if (numberNode != null) {
            return Integer.valueOf(numberNode.getNumber().getToken());
        } else {
            return expNode.calculateValue(symbolTable);
        }
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (lValNode != null) {
            sb.append(lValNode.print());
        } else if (numberNode != null) {
            sb.append(numberNode.print());
        } else {
            sb.append(lparent.toString());
            sb.append(expNode.print());
            sb.append(rparent.toString());
        }
        sb.append(name).append("\n");
        return sb.toString();
    }

    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        if (lValNode != null) {
            return lValNode.getSymbolType(symbolTable);
        } else if (numberNode != null) {
            return SymbolType.VAR;
        } else {
            return expNode.getSymbolType(symbolTable);
        }
    }
}

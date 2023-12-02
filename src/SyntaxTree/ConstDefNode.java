package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class ConstDefNode extends TreeNode {
    private String name = "<ConstDef>";
    private Token ident;
    private ArrayList<Token> lbrack;
    private ArrayList<Token> rbrack;
    private ArrayList<ConstExpNode> constExpNodes;
    private Token assign;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token ident) {
        this.ident = ident;
        constExpNodes = new ArrayList<>();
        lbrack = new ArrayList<>();
        rbrack = new ArrayList<>();
    }

    public void addLbrack(Token lbrack) {
        this.lbrack.add(lbrack);
    }

    public void addConstExpNode(ConstExpNode constExpNode) {
        constExpNodes.add(constExpNode);
    }

    public void addRbrack(Token rbrack) {
        this.rbrack.add(rbrack);
    }

    public void addAssign(Token assign) {
        this.assign =assign;
    }

    public void addConstInitValNode(ConstInitValNode constInitValNode) {
        this.constInitValNode = constInitValNode;
    }

    public Token getIdent() {
        return this.ident;
    }

    public int getLBrackNum() {
        return lbrack.size();
    }

    public int getRBrackNum() {
        return rbrack.size();
    }

    public ConstInitValNode getConstInitValNode() {
        return this.constInitValNode;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        for (int i = 0;i < lbrack.size();i++) {
            sb.append(lbrack.get(i).toString());
            sb.append(constExpNodes.get(i).print());
            sb.append(rbrack.get(i).toString());
        }
        sb.append(assign.toString());
        sb.append(constInitValNode.print());
        sb.append(name);
        sb.append("\n");
        return sb.toString();
    }
}

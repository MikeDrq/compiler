package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class ConstInitValNode extends TreeNode {
    private String name = "<ConstInitVal>";
    private ConstExpNode constExpNode;
    private Token lbrace;
    private Token rbrace;
    private ArrayList<Token> comma;
    private ArrayList<ConstInitValNode> constInitValNodes;

    public ConstInitValNode() {
        this.comma = new ArrayList<>();
        this.constInitValNodes = new ArrayList<>();
    }

    public void addLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void addRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public void addComma(Token comma) {
        this.comma.add(comma);
    }

    public void addConstInitValNode(ConstInitValNode constInitValNode) {
        this.constInitValNodes.add(constInitValNode);
    }

    public void addConstExpNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public ArrayList<ConstInitValNode> getConstInitValNodes() {
        return constInitValNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (constExpNode != null) {
            sb.append(constExpNode.print());
        } else {
            sb.append(lbrace.toString());
            sb.append(constInitValNodes.get(0).print());
            if (comma.size() > 0) {
                for (int i = 1;i < constInitValNodes.size();i++) {
                    sb.append(comma.get(i-1).toString());
                    sb.append(constInitValNodes.get(i).print());
                }
            }
            sb.append(rbrace.toString());
        }
        sb.append(name);
        sb.append("\n");
        return sb.toString();
    }
}

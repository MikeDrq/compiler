package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class InitValNode extends TreeNode {
    private String name = "<InitVal>";
    private Token lbrace;
    private ArrayList<InitValNode> initValNodes;
    private ArrayList<Token> comma;
    private Token rbrace;
    private ExpNode expNode;

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public InitValNode() {
        this.initValNodes = new ArrayList<>();
        this.comma = new ArrayList<>();
    }

    public ArrayList<InitValNode> getInitValNodes() {
        return this.initValNodes;
    }

    public void addLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void addInitValNode(InitValNode initValNode) {
        this.initValNodes.add(initValNode);
    }

    public void addComma(Token comma) {
        this.comma.add(comma);
    }

    public void addRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (expNode != null) {
            sb.append(expNode.print());
        } else {
            sb.append(lbrace.toString());
            sb.append(initValNodes.get(0).print());
            if (comma.size() > 0) {
                for (int i = 1;i < initValNodes.size();i++) {
                    sb.append(comma.get(i-1).toString());
                    sb.append(initValNodes.get(i).print());
                }
            }
            sb.append(rbrace.toString());
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

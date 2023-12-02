package SyntaxTree;

import Lexer.Token;

public class ForStmtNode extends TreeNode {
    private String name = "<ForStmt>";
    private LValNode lValNode;
    private Token assign;
    private ExpNode expNode;


    public void addlValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void addAssign(Token assign) {
        this.assign = assign;
    }

    public void addExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public ExpNode getExpNode() {
        return this.expNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(lValNode.print());
        sb.append(assign.toString());
        sb.append(expNode.print());
        sb.append(name);
        sb.append("\n");
        return sb.toString();
    }
}

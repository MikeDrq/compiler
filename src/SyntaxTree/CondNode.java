package SyntaxTree;

import Lexer.Token;

public class CondNode extends TreeNode {
    private String name = "<Cond>";
    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return this.lOrExpNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(lOrExpNode.print());
        sb.append(name);
        sb.append("\n");
        return sb.toString();
    }
}

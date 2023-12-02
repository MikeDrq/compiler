package SyntaxTree;

import Lexer.Token;

public class UnaryOpNode extends TreeNode{
    private String name = "<UnaryOp>";
    private Token op;

    public UnaryOpNode(Token token) {
        this.op = token;
    }

    public Token getUnaryOpNode() {
        return op;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(op.toString());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

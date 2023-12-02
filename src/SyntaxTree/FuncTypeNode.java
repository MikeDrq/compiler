package SyntaxTree;

import Lexer.Token;

public class FuncTypeNode extends TreeNode {
    private String name = "<FuncType>";
    private Token token;

    public FuncTypeNode(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return this.token;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

package SyntaxTree;

import Lexer.Token;

public class NumberNode extends TreeNode {
    private String name = "<Number>";
    private Token number;

    public NumberNode(Token number) {
        this.number = number;
    }

    public Token getNumber() {
        return number;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(number.toString());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

package SyntaxTree;

import Lexer.Token;
import Lexer.TokenType;

public class BTypeNode extends TreeNode {
    private Token token;

    public BTypeNode(Token token) {
        this.token = token;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}

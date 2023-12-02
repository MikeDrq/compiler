package Lexer;

public class Token {
    private String token;
    private int line;
    private int number;
    private TokenType tokenType;

    public Token(String token,int line,int number,TokenType tokenType) {
        this.token = token;
        this.line = line;
        this.number = number;
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getToken() {
        return token;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return tokenType + " " + token + "\n";
    }
}

package Lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Lexer {
    private String source;
    private int curPos;
    private String token;
    private String tokenType;
    private int curLine;
    private int number;
    private ArrayList<Token> tokenList;
    private HashMap<String,TokenType> reserveWords;
    public Lexer(String source) {
        this.source = source;
        this.curPos = 0;
        this.token = "";
        this.tokenType = "";
        this.curLine = 1;
        this.number = 0;
        this.tokenList = new ArrayList<>();
        this.reserveWords = new HashMap<>();
        reserveWords.put("main",TokenType.MAINTK);
        reserveWords.put("const",TokenType.CONSTTK);
        reserveWords.put("int",TokenType.INTTK);
        reserveWords.put("break",TokenType.BREAKTK);
        reserveWords.put("continue",TokenType.CONTINUETK);
        reserveWords.put("if",TokenType.IFTK);
        reserveWords.put("else",TokenType.ELSETK);
        reserveWords.put("for",TokenType.FORTK);
        reserveWords.put("getint",TokenType.GETINTTK);
        reserveWords.put("printf",TokenType.PRINTFTK);
        reserveWords.put("return",TokenType.RETURNTK);
        reserveWords.put("void",TokenType.VOIDTK);
    }

    public ArrayList<Token> analysis() {
        while (!endOfFile()) {
            clearToken();
            while (isSpace() || isNewLine() || isTab()) {
                curPos++;
            }
            if (endOfFile()) {
                break;
            }
            if (isIdentifierNonDigit()) {
                while (isIdentifierNonDigit() || isDigit()) {
                    token = token + source.charAt(curPos);
                    curPos++;
                }
                if (reserveWords.containsKey(token)) {
                    tokenList.add(new Token(token, curLine, 0, reserveWords.get(token)));
                } else {
                    tokenList.add(new Token(token, curLine, 0, TokenType.IDENFR));
                }
            } else if (isDigit()) {
                while (isDigit()) {
                    number = (source.charAt(curPos) - '0') + number * 10;
                    curPos++;
                }
                token = String.valueOf(number);
                tokenList.add(new Token(token, curLine, number, TokenType.INTCON));
                number = 0;
            } else if (!endOfFile() && source.charAt(curPos) == '"') {
                token = token + source.charAt(curPos);
                curPos++;
                int flag = 1;
                while (source.charAt(curPos) != '"') {
                    token = token + source.charAt(curPos);
                    curPos++;
                }
                token = token + source.charAt(curPos);
                curPos++;
                tokenList.add(new Token(token, curLine, number, TokenType.STRCON));
            } else if (isNot()) {
                token = token + "!";
                curPos++;
                if (isEql()) {
                    token = token + "=";
                    curPos++;
                    tokenList.add(new Token(token, curLine, number, TokenType.NEQ));
                } else {
                    tokenList.add(new Token(token, curLine, number, TokenType.NOT));
                }
            } else if (isAnd()) {
                token = token + "&";
                curPos++;
                if (isAnd()) {
                    token = token + "&";
                    curPos++;
                    tokenList.add(new Token(token,curLine,number,TokenType.AND));
                } else {
                    tokenList.add(new Token(token,curLine,number,null));
                }
            } else if (isOr()) {
                token = token + "|";
                curPos++;
                if (isOr()) {
                    token = token + "|";
                    curPos++;
                    tokenList.add(new Token(token,curLine,number,TokenType.OR));
                } else {
                    tokenList.add(new Token(token,curLine,number,null));
                }
            } else if (isPlus()) {
                token = "+";
                tokenList.add(new Token(token,curLine,0,TokenType.PLUS));
                curPos++;
            } else if (isMinu()) {
                token = "-";
                tokenList.add(new Token(token,curLine,0,TokenType.MINU));
                curPos++;
            } else if (isMult()) {
                token = "*";
                tokenList.add(new Token(token,curLine,0,TokenType.MULT));
                curPos++;
            } else if (isDiv()) {
                token = "/";
                curPos++;
                if (isDiv()) {
                    commentIsSingle();
                } else if (isMult()) {
                    commentIsMult();
                } else {
                    tokenList.add(new Token(token,curLine,0,TokenType.DIV));
                }
            } else if (isMod()) {
                token = "%";
                tokenList.add(new Token(token,curLine,0,TokenType.MOD));
                curPos++;
            } else if (isLss()) {
                token = "<";
                curPos++;
                if (isEql()) {
                    token = token + "=";
                    curPos++;
                    tokenList.add(new Token(token, curLine, 0, TokenType.LEQ));
                } else {
                    tokenList.add(new Token(token, curLine, 0, TokenType.LSS));
                }
            } else if (isGre()) {
                token = ">";
                curPos++;
                if (isEql()) {
                    token = token + "=";
                    curPos++;
                    tokenList.add(new Token(token, curLine, 0, TokenType.GEQ));
                } else {
                    tokenList.add(new Token(token, curLine, 0, TokenType.GRE));
                }
            } else if (isEql()) {
                token = "=";
                curPos++;
                if (isEql()) {
                    token = token + "=";
                    curPos++;
                    tokenList.add(new Token(token, curLine, 0, TokenType.EQL));
                } else {
                    tokenList.add(new Token(token, curLine, 0, TokenType.ASSIGN));
                }
            } else if (isSemicn()) {
                token = ";";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.SEMICN));
            } else if (isComma()) {
                token = ",";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.COMMA));
            } else if (isLparent()) {
                token = "(";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.LPARENT));
            } else if (isRparent()) {
                token = ")";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.RPARENT));
            } else if (isLbrack()) {
                token = "[";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.LBRACK));
            } else if (isRbrack()) {
                token = "]";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.RBRACK));
            } else if (isLbrace()) {
                token = "{";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.LBRACE));
            } else if (isRbrace()) {
                token = "}";
                curPos++;
                tokenList.add(new Token(token,curLine,0,TokenType.RBRACE));
            } else {
                token = token + source.charAt(curPos);
                curPos++;
                tokenList.add(new Token(token,curLine,0,null));
            }
        }
        return this.tokenList;
    }

    public void clearToken() {
        token = "";
    }

    private boolean isSpace() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == ' ') {
            return true;
        }
        return false;
    }

    private boolean isNewLine() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '\n') {
            curLine++;
            return true;
        }
        return false;
    }

    private boolean isTab() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '\t') {
            return true;
        }
        return false;
    }

    private boolean isIdentifierNonDigit() {
        if (endOfFile()) {
            return false;
        }
        char c = source.charAt(curPos);
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
            return true;
        }
        return false;
    }

    private boolean isDigit() {
        if (endOfFile()) {
            return false;
        }
        char c = source.charAt(curPos);
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    private boolean endOfFile() {
        if (curPos < source.length()) {
            return false;
        }
        return true;
    }

    private boolean isNot() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '!') {
            return true;
        }
        return false;
    }

    private boolean isAnd() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '&') {
            return true;
        }
        return false;
    }

    private boolean isOr() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '|') {
            return true;
        }
        return false;
    }

    private boolean isPlus() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '+') {
            return true;
        }
        return false;
    }

    private boolean isMinu() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '-') {
            return true;
        }
        return false;
    }

    private boolean isMult() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '*') {
            return true;
        }
        return false;
    }

    private boolean isDiv() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '/') {
            return true;
        }
        return false;
    }

    private boolean isMod() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '%') {
            return true;
        }
        return false;
    }

    private boolean isLss() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '<') {
            return true;
        }
        return false;
    }

    private boolean isGre() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '>') {
            return true;
        }
        return false;
    }

    private boolean isEql() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '=') {
            return true;
        }
        return false;
    }

    private boolean isSemicn() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == ';') {
            return true;
        }
        return false;
    }

    private boolean isComma() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == ',') {
            return true;
        }
        return false;
    }

    private boolean isLparent() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '(') {
            return true;
        }
        return false;
    }

    private boolean isRparent() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == ')') {
            return true;
        }
        return false;
    }

    private boolean isLbrack() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '[') {
            return true;
        }
        return false;
    }

    private boolean isRbrack() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == ']') {
            return true;
        }
        return false;
    }

    private boolean isLbrace() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '{') {
            return true;
        }
        return false;
    }

    private boolean isRbrace() {
        if (endOfFile()) {
            return false;
        }
        if (source.charAt(curPos) == '}') {
            return true;
        }
        return false;
    }
    
    public void commentIsSingle() {
        while(!endOfFile() && !isNewLine()) {
            curPos++;
        }
        curPos++;
    }

    public void commentIsMult() {
        while(!endOfFile()) {
            if (curPos < source.length() - 1) {
                isNewLine();
                if (source.charAt(curPos) == '*' && source.charAt(curPos + 1) == '/') {
                    curPos++;
                    curPos++;
                    break;
                }
                curPos++;
            } else {
                curPos += 2;
                System.out.println("多行注释错误");
                break;
            }
        }
    }
}

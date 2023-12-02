package SyntaxTree;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;

import java.util.ArrayList;

public class MulExpNode extends TreeNode {
    private String name = "<MulExp>";
    private ArrayList<UnaryExpNode> unaryExpNodes;
    private ArrayList<Token> op;

    public MulExpNode() {
        this.unaryExpNodes = new ArrayList<>();
        this.op = new ArrayList<>();
    }

    public void addUnaryExpNode(UnaryExpNode unaryExpNode) {
        this.unaryExpNodes.add(unaryExpNode);
    }

    public void addOp(Token token) {
        this.op.add(token);
    }

    public int calculateValue(SymbolTable symbolTable) {
        int ans = unaryExpNodes.get(0).calculateValue(symbolTable);
        for (int i = 0;i < op.size();i++) {
            if (op.get(i).getTokenType() == TokenType.MULT) {
                ans = ans * unaryExpNodes.get(i+1).calculateValue(symbolTable);
            } else if (op.get(i).getTokenType() == TokenType.DIV) {
                ans = ans / unaryExpNodes.get(i+1).calculateValue(symbolTable);
            } else if (op.get(i).getTokenType() == TokenType.MOD) {
                ans = ans % unaryExpNodes.get(i+1).calculateValue(symbolTable);
            }
        }
        return ans;
    }

    public ArrayList<Token> getOp() {
        return op;
    }

    public ArrayList<UnaryExpNode> getUnaryExpNodes() {
        return unaryExpNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryExpNodes.get(0).print());
        sb.append(name).append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < unaryExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(unaryExpNodes.get(i).print());
                sb.append(name).append("\n");
            }
        }
        return sb.toString();
    }
    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        if (op.size() > 0) {
            SymbolType temp = null;
            for (int i = 0;i < unaryExpNodes.size();i++) {
                SymbolType now = unaryExpNodes.get(i).getSymbolType(symbolTable);
                if (now == null) {
                    return null; //之前有错误
                } else if (now == SymbolType.VAR) {
                    temp = SymbolType.VAR;
                } else if (now == SymbolType.VAR_ARRAY1) {
                    temp = SymbolType.VAR_ARRAY1;
                } else if (now == SymbolType.VAR_ARRAY2) {
                    temp = SymbolType.VAR_ARRAY2;
                } else {
                    temp = null;
                }
            }
            return temp;
        } else {
            return unaryExpNodes.get(0).getSymbolType(symbolTable);
        }
    }
}

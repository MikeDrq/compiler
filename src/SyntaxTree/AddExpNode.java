package SyntaxTree;
import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;

import java.util.ArrayList;

public class AddExpNode extends TreeNode {
    private String name = "<AddExp>";
    private ArrayList<MulExpNode> mulExpNodes;
    private ArrayList<Token> op;

    public AddExpNode() {
        this.mulExpNodes = new ArrayList<>();
        this.op = new ArrayList<>();
    }

    public void addAddExpNode(MulExpNode mulExpNode) {
        this.mulExpNodes.add(mulExpNode);
    }

    public void addOp(Token token) {
        this.op.add(token);
    }

    public int calculateValue(SymbolTable symbolTable) {
        int ans = mulExpNodes.get(0).calculateValue(symbolTable);
        for (int i = 0;i < op.size();i++) {
            if (op.get(i).getTokenType() == TokenType.PLUS) {
                ans = ans + mulExpNodes.get(i+1).calculateValue(symbolTable);
            } else if (op.get(i).getTokenType() == TokenType.MINU) {
                ans = ans - mulExpNodes.get(i+1).calculateValue(symbolTable);
            }
        }
        return ans;
    }

    public ArrayList<MulExpNode> getMulExpNodes() {
        return this.mulExpNodes;
    }

    public ArrayList<Token> getOp() {
        return this.op;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(mulExpNodes.get(0).print());
        sb.append(name).append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < mulExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(mulExpNodes.get(i).print());
                sb.append(name).append("\n");
            }
        }
        return sb.toString();      
    }

    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        if (op.size() > 0) {
            SymbolType temp = null;
            for (int i = 0;i < mulExpNodes.size();i++) {
                SymbolType now = mulExpNodes.get(i).getSymbolType(symbolTable);
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
            return mulExpNodes.get(0).getSymbolType(symbolTable);
        }
    }
}

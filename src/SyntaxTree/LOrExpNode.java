package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class LOrExpNode extends TreeNode{
    private String name = "<LOrExp>";
    private ArrayList<LAndExpNode> lAndExpNodes;
    private ArrayList<Token> op;

    public LOrExpNode() {
        lAndExpNodes = new ArrayList<>();
        op = new ArrayList<>();
    }

    public void addLAndExpNode(LAndExpNode lAndExpNode) {
        lAndExpNodes.add(lAndExpNode);
    }

    public void addOp(Token op) {
        this.op.add(op);
    }

    public ArrayList<LAndExpNode> getlAndExpNodes() {
        return lAndExpNodes;
    }

    public ArrayList<Token> getOp() {
        return op;
    }
    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(lAndExpNodes.get(0).print());
        sb.append(name).append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < lAndExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(lAndExpNodes.get(i).print());
                sb.append(name).append("\n");
            }
        }
        return sb.toString();
    }
}

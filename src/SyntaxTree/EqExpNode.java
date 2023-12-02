package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class EqExpNode extends TreeNode{
    private String name = "<EqExp>";
    private ArrayList<RelExpNode> relExpNodes;
    private ArrayList<Token> op;

    public EqExpNode() {
        relExpNodes = new ArrayList<>();
        op = new ArrayList<>();
    }

    public void addRelExpNode(RelExpNode relExpNode) {
        relExpNodes.add(relExpNode);
    }

    public void addOp(Token op) {
        this.op.add(op);
    }

    public ArrayList<RelExpNode> getRelExpNodes() {
        return relExpNodes;
    }

    public ArrayList<Token> getOp() {
        return op;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(relExpNodes.get(0).print());
        sb.append(name);
        sb.append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < relExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(relExpNodes.get(i).print());
                sb.append(name);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

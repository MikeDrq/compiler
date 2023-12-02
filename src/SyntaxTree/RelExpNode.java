package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class RelExpNode extends TreeNode {
    private String name = "<RelExp>";
    private ArrayList<AddExpNode> addExpNodes;
    private ArrayList<Token> op;

    public RelExpNode() {
        addExpNodes = new ArrayList<>();
        op = new ArrayList<>();
    }

    public void addAddExpNode(AddExpNode addExpNode) {
        addExpNodes.add(addExpNode);
    }

    public void addOp(Token op) {
        this.op.add(op);
    }

    public ArrayList<AddExpNode> getAddExpNodes() {
        return addExpNodes;
    }

    public ArrayList<Token> getOp() {
        return op;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNodes.get(0).print());
        sb.append(name).append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < addExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(addExpNodes.get(i).print());
                sb.append(name).append("\n");
            }
        }
        return sb.toString();
    }
}

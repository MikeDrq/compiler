package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class LAndExpNode extends TreeNode{
    private String name = "<LAndExp>";
    private ArrayList<EqExpNode> eqExpNodes;
    private ArrayList<Token> op;

    public LAndExpNode() {
        eqExpNodes = new ArrayList<>();
        op = new ArrayList<>();
    }

    public void addEqExpNode(EqExpNode eqExpNode) {
        eqExpNodes.add(eqExpNode);
    }

    public void addOp(Token op) {
        this.op.add(op);
    }

    public ArrayList<EqExpNode> getEqExpNodes() {
        return eqExpNodes;
    }

    public ArrayList<Token> getOp() {
        return op;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(eqExpNodes.get(0).print());
        sb.append(name).append("\n");
        if (op.size() > 0) {
            for (int i = 1;i < eqExpNodes.size();i++) {
                sb.append(op.get(i-1).toString());
                sb.append(eqExpNodes.get(i).print());
                sb.append(name).append("\n");
            }
        }
        return sb.toString();
    }
}

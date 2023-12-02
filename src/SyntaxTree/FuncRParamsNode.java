package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class FuncRParamsNode extends TreeNode {
    private String name = "<FuncRParams>";
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> commas;

    public FuncRParamsNode() {
        this.expNodes = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public void addExpNodes(ExpNode expNode) {
        expNodes.add(expNode);
    }

    public void addComma(Token comma) {
        commas.add(comma);
    }

    public int getParamsNumber() {
        return expNodes.size();
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(expNodes.get(0).print());
        if (commas.size() >0 ){
            for (int i = 1;i < expNodes.size();i++) {
                sb.append(commas.get(i-1).toString());
                sb.append(expNodes.get(i).print());
            }
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

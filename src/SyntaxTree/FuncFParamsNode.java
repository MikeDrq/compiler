package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class FuncFParamsNode extends TreeNode {
    private String name = "<FuncFParams>";
    private ArrayList<FuncFParamNode> funcFParamNodes;
    private ArrayList<Token> comma;

    public FuncFParamsNode() {
        this.funcFParamNodes = new ArrayList<>();
        this.comma = new ArrayList<>();
    }

    public void addFuncFParamNode(FuncFParamNode funcFParamNode) {
        this.funcFParamNodes.add(funcFParamNode);
    }

    public void addComma(Token comma) {
        this.comma.add(comma);
    }

    public ArrayList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcFParamNodes.get(0).print());
        if (comma.size() > 0) {
            for (int i = 1;i < funcFParamNodes.size();i++) {
                sb.append(comma.get(i-1).toString());
                sb.append(funcFParamNodes.get(i).print());
            }
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

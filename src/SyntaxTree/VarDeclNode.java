package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class VarDeclNode extends TreeNode {
    private String name = "<VarDecl>";
    private BTypeNode bTypeNode;
    private ArrayList<VarDefNode> varDefNodes;
    private ArrayList<Token> comma;
    private Token semicn;

    public VarDeclNode(BTypeNode bTypeNode) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = new ArrayList<>();
        this.comma = new ArrayList<>();
    }

    public void addVarDefNode(VarDefNode varDefNode) {
        this.varDefNodes.add(varDefNode);
    }

    public void addComma(Token comma) {
        this.comma.add(comma);
    }

    public void addSemicn(Token semicn) {
        this.semicn = semicn;
    }

    public ArrayList<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.print());
        sb.append(varDefNodes.get(0).print());
        if (comma.size() > 0) {
            for (int i = 1;i < varDefNodes.size();i++) {
                sb.append(comma.get(i-1).toString());
                sb.append(varDefNodes.get(i).print());
            }
        }
        sb.append(semicn.toString());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class ConstDeclNode extends TreeNode {
    private Token cons;
    private BTypeNode bTypeNode;
    private ArrayList<ConstDefNode> constDefNodes;
    private ArrayList<Token> comma;
    private Token semicn;
    private String name = "<ConstDecl>";

    public ConstDeclNode(Token cons,BTypeNode bTypeNode) {
        this.cons = cons;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = new ArrayList<>();
        this.comma = new ArrayList<>();
    }

    public void add(ConstDefNode constDefNode) {
        this.constDefNodes.add(constDefNode);
    }

    public void addToken(Token comma) {
        this.comma.add(comma);
    }

    public void addSemicn(Token semicn) {
        this.semicn = semicn;
    }

    public ArrayList<ConstDefNode> getConstDefNodes() {
        return this.constDefNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(cons.toString());
        sb.append(bTypeNode.print());
        if (comma.size() == 0) {
            sb.append(constDefNodes.get(0).print());
        } else {
            sb.append(constDefNodes.get(0).print());
            for (int i = 1;i < constDefNodes.size();i++) {
                sb.append(comma.get(i-1).toString());
                sb.append(constDefNodes.get(i).print());
            }
        }
        sb.append(semicn.toString());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

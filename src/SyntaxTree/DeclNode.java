package SyntaxTree;

public class DeclNode extends TreeNode {
    private boolean isConst;
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;
    private String name = "<Decl>";
    public DeclNode(ConstDeclNode constDeclNode) {
        this.constDeclNode = constDeclNode;
        isConst = true;
    }

    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
        isConst = false;
    }

    public boolean queryConst() {
        return isConst;
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }
    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (isConst) {
            sb.append(constDeclNode.print());
        } else {
            sb.append(varDeclNode.print());
        }
        return sb.toString();
    }
}

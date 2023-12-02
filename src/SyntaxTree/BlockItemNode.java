package SyntaxTree;

public class BlockItemNode extends TreeNode{
    private DeclNode declNode;
    private StmtNode stmtNode;
    private boolean isDeclNode;
    private String name = "<BlockItem>";

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
        this.isDeclNode = true;
    }

    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
        this.isDeclNode = false;
    }

    public boolean queryIsDeclNode() {
        return isDeclNode;
    }

    public DeclNode getDeclNode() {
        return declNode;
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (isDeclNode) {
            sb.append(declNode.print());
        } else {
            sb.append(stmtNode.print());
        }
        return sb.toString();
    }
}

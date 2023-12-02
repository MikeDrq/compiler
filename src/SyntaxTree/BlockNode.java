package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class BlockNode extends TreeNode {
    private String name = "<Block>";
    private Token lbrace;
    private Token rbrace;
    private ArrayList<BlockItemNode> blockItemNodes;

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    public void addLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void addRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public void addBlockItemNode(BlockItemNode blockItemNode) {
        this.blockItemNodes.add(blockItemNode);
    }

    public ArrayList<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(lbrace.toString());
        if (blockItemNodes.size() > 0) {
            for (BlockItemNode blockItemNode : blockItemNodes) {
                sb.append(blockItemNode.print());
            }
        }
        sb.append(rbrace.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}

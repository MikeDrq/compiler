package SyntaxTree;

import Lexer.Token;

public class MainFuncDefNode extends TreeNode {
    private String name = "<MainFuncDef>";
    private Token wordInt;
    private Token wordMain;
    private Token lparent;
    private Token rparent;
    private BlockNode blockNode;

    public void addWordInt(Token wordInt) {
        this.wordInt = wordInt;
    }

    public void addWordMain(Token wordMain) {
        this.wordMain = wordMain;
    }

    public void addLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void addRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void addBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public Token getMain() {
        return wordMain;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(wordInt.toString());
        sb.append(wordMain.toString());
        sb.append(lparent.toString());
        sb.append(rparent.toString());
        sb.append(blockNode.print());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

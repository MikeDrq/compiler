package SyntaxTree;

import Lexer.Token;
import Lexer.TokenType;

public class FuncDefNode extends TreeNode {
    private String name = "<FuncDef>";
    private FuncTypeNode funcTypeNode;
    private Token ident;
    private Token lparent;
    private Token rparent;
    private FuncFParamsNode funcFParamsNode;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode,Token ident) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
    }

    public void addLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void addRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void addFuncFParamsNode(FuncFParamsNode funcFParamsNode) {
        this.funcFParamsNode = funcFParamsNode;
    }

    public void addBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public Token getFuncType() {
        return funcTypeNode.getToken();
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public Token getIdent() {
        return this.ident;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcTypeNode.print());
        sb.append(ident.toString());
        sb.append(lparent.toString());
        if (funcFParamsNode != null) {
            sb.append(funcFParamsNode.print());
        }
        sb.append(rparent.toString());
        sb.append(blockNode.print());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

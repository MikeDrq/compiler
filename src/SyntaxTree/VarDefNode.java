package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class VarDefNode extends TreeNode {
    private String name = "<VarDef>";
    private Token ident;
    private ArrayList<Token> lbracks;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rbracks;
    private Token assign;
    private InitValNode initValNode;

    public VarDefNode(Token ident) {
        this.ident = ident;
        this.lbracks = new ArrayList<>();
        this.rbracks = new ArrayList<>();
        this.constExpNodes = new ArrayList<>();
    }

    public void addLbrack(Token lbrack) {
        lbracks.add(lbrack);
    }

    public void addConstExpNode(ConstExpNode constExpNode) {
        this.constExpNodes.add(constExpNode);
    }

    public void addRbrack(Token rbrack) {
        rbracks.add(rbrack);
    }

    public void addAsign(Token assign) {
        this.assign = assign;
    }

    public void addInitValNode(InitValNode initValNode) {
        this.initValNode = initValNode;
    }

    public Token getIdent() {
        return this.ident;
    }

    public int getLBrackNum() {
        return lbracks.size();
    }

    public InitValNode getInitValNode() {
        return this.initValNode;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return this.constExpNodes;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (lbracks.size() > 0) {
            for (int i = 0;i < lbracks.size();i++) {
                sb.append(lbracks.get(i).toString());
                sb.append(constExpNodes.get(i).print());
                sb.append(rbracks.get(i).toString());
            }
        }
        if (assign != null) {
            sb.append(assign.toString());
            sb.append(initValNode.print());
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

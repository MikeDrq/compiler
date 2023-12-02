package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;

public class FuncFParamNode extends TreeNode {
    private String name = "<FuncFParam>";
    private BTypeNode bTypeNode;
    private Token ident;
    private Token flparent;
    private Token frparent;
    private ArrayList<Token> lparent;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rparent;
    public FuncFParamNode(BTypeNode bTypeNode) {
        this.bTypeNode = bTypeNode;
        this.lparent = new ArrayList<>();
        this.constExpNodes = new ArrayList<>();
        this.rparent = new ArrayList<>();
    }

    public void addIdent(Token ident) {
        this.ident = ident;
    }

    public void addFlparent(Token flparent) {
        this.flparent = flparent;
    }

    public void addFrparent(Token frparent) {
        this.frparent = frparent;
    }

    public void addLparent(Token lparent) {
        this.lparent.add(lparent);
    }

    public void addConstExpNode(ConstExpNode constExpNode) {
        this.constExpNodes.add(constExpNode);
    }

    public void addRparent(Token rparent) {
        this.rparent.add(rparent);
    }

    public ArrayList<ConstExpNode> getConstExpNodes () {
        return this.constExpNodes;
    }

    public int getLParentNum() {
        int cnt = 0;
        if (flparent == null) {
            return 0;
        } else {
            cnt++;
            cnt = cnt + lparent.size();
            return cnt;
        }
    }

    public Token getToken() {
        return ident;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.print());
        sb.append(ident.toString());
        if (flparent != null ) {
            sb.append(flparent.toString());
            sb.append(frparent.toString());
        }
        if (lparent.size() > 0) {
            for (int i = 0;i < lparent.size();i++) {
                sb.append(lparent.get(i).toString());
                sb.append(constExpNodes.get(i).print());
                sb.append(rparent.get(i).toString());
            }
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

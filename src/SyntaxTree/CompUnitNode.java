package SyntaxTree;

import java.util.ArrayList;

public class CompUnitNode extends TreeNode {
    private ArrayList<DeclNode> declNodes = new ArrayList<>();
    private ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
    private MainFuncDefNode mainFuncDefNode = null;
    private String name = "<CompUnit>";

    public void addDeclNode(DeclNode declNode) {
        declNodes.add(declNode);
    }

    public void addFuncDefNode(FuncDefNode funcDefNode) {
        funcDefNodes.add(funcDefNode);
    }

    public void addMainFuncDefNode(MainFuncDefNode mainFuncDefNode) {
        this.mainFuncDefNode = mainFuncDefNode;
    }

    public ArrayList<DeclNode> getDeclNodes() {
        return this.declNodes;
    }

    public ArrayList<FuncDefNode> getFuncDefNodes() {
        return this.funcDefNodes;
    }

    public MainFuncDefNode getMainFuncDefNode() {
        return this.mainFuncDefNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (declNodes != null) {
            for (DeclNode declNode : declNodes) {
                sb.append(declNode.print());
            }
        }
        if (funcDefNodes != null) {
            for (FuncDefNode funcDefNode:funcDefNodes) {
                sb.append(funcDefNode.print());
            }
        }
        sb.append(mainFuncDefNode.print());
        sb.append(name + "\n");
        return sb.toString();
    }
}

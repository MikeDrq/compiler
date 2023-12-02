package SyntaxTree;

import SymbolTable.SymbolTable;

public class ConstExpNode extends TreeNode {
    private String name = "<ConstExp>";
    private AddExpNode addExpNode;

    public void addConstExp(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public int calcuateValue(SymbolTable symbolTable) {
        return addExpNode.calculateValue(symbolTable);
    }

    public AddExpNode getAddExpNode() {
        return this.addExpNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNode.print());
        sb.append(name).append("\n");
        return sb.toString();
    }
}

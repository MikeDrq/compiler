package SyntaxTree;

import SymbolTable.SymbolTable;
import SymbolTable.SymbolType;

public class ExpNode extends TreeNode {
    private String name = "<Exp>";
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public int calculateValue(SymbolTable symbolTable) {
        return addExpNode.calculateValue(symbolTable);
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNode.print());
        sb.append(name);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public SymbolType getSymbolType(SymbolTable symbolTable) {
        return addExpNode.getSymbolType(symbolTable);
    }
}

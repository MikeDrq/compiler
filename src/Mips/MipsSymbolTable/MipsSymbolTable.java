package Mips.MipsSymbolTable;

import java.util.HashMap;

public class MipsSymbolTable {
    private String name;
    private MipsSymbol mipsSymbol;

    private HashMap<String,MipsSymbol> mipsSymbolTable;

    public MipsSymbolTable() {
        mipsSymbolTable = new HashMap<>();
    }

    public void addMipsSymbol(String name,MipsSymbol mipsSymbol) {
        mipsSymbolTable.put(name,mipsSymbol);
    }

    public Boolean containsMipsSymbol(String name) {
        return mipsSymbolTable.containsKey(name);
    }

    public MipsSymbol getMipsSymbol(String name) {
        return mipsSymbolTable.get(name);
    }
}

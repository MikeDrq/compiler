package SymbolTable;

public enum SymbolType {
    VOID, //只有在判断函数实参时用到
    VAR,
    VAR_ARRAY1, //一维数组
    VAR_ARRAY2, //二维数组
    CONST,
    CONST_ARRAY1,
    CONST_ARRAY2,
    FUNC
}

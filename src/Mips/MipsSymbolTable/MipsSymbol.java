package Mips.MipsSymbolTable;

import Middle.LlvmIrValue;

public class MipsSymbol {
    private String name;
    private int offset; //内存中的位置 sp
    private LlvmIrValue llvmIrValue;
    private int fp_offset = 0; //数组
    private Boolean useRelative = true;
    private Boolean usePos = false;

    public MipsSymbol (String name,int offset,LlvmIrValue llvmIrValue) {
        this.name = name;
        this.offset = offset;
        this.llvmIrValue = llvmIrValue;
    }

    public LlvmIrValue getLlvmIrValue() {
        return llvmIrValue;
    }

    public MipsSymbol(String name, LlvmIrValue llvmIrValue) {
        this.name = name;
        this.llvmIrValue = llvmIrValue;
    }

    public void setUsePos(Boolean usePos) {
        this.usePos = usePos;
    }

    public Boolean getUsePos() {
        return usePos;
    }

    public void setFpOffset(int fp_offset) {
        this.fp_offset = fp_offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getFp_offset() {
        return this.fp_offset;
    }

    public Boolean isRelative() {
        return useRelative;
    }

    public void changeToAbs() {
        useRelative = false;
    }
}

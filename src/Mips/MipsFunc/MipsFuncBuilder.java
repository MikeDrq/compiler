package Mips.MipsFunc;

import Middle.LlvmIrValue;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Mips.GlobalLabelCnt;
import Mips.MipsBasicBlock.MipsBasicBlockBuilder;
import Mips.MipsSymbolTable.MipsSymbol;
import Mips.MipsSymbolTable.MipsSymbolTable;
import Mips.Register;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsFuncBuilder {
    private Func func;
    private StringCnt stringCnt;
    private MipsSymbolTable mipsSymbolTable;
    private HashMap<String,Integer> labelMatch;
    private GlobalLabelCnt gbc;


    public MipsFuncBuilder(Func func,StringCnt stringCnt,GlobalLabelCnt gbc) {
        this.func = func;
        this.stringCnt = stringCnt;
        mipsSymbolTable = new MipsSymbolTable();
        labelMatch = new HashMap<>();
        this.gbc = gbc;
        setParams();
    }

    public void setParams() {
        ArrayList<LlvmIrValue> params = func.getParams();
        int temp = 8; //第一个参数，4 是 $ra，0 是当前函数的起始位置。
        for (int i = 0;i < params.size();i++) {
            LlvmIrValue param = params.get(i);
            MipsSymbol mipsSymbol = new MipsSymbol("%v_" + i,temp,param);
            temp = temp + 4;
            mipsSymbolTable.addMipsSymbol("%v_" + i,mipsSymbol);
            //System.out.println(param.getDim());
        }
    }

    public MipsFunc generateMipsFuncs(ArrayList<String> strings) {
        MipsFunc mipsFunc; //用V0 存储返回值
        Boolean isMain;
        if (this.func.getName().equals("@main")) {
            mipsFunc = new MipsFunc("main");
            isMain = true;
        } else {
            mipsFunc = new MipsFunc(func.getName().substring(1)); //函数传参暂时全用内存
            isMain = false;
        }
        ArrayList<BasicBlock> basicBlocks = func.getBasicBlocks();
        Register register = new Register();
        int offset = 0; //函数内相对于 $sp 的偏移
        int fp_offset = 0; //$fp
        for (BasicBlock basicBlock : basicBlocks) {
            if (!basicBlock.getName().equals("-1")) {
                MipsBasicBlockBuilder mipsBasicBlockBuilder = new MipsBasicBlockBuilder(basicBlock, stringCnt, mipsSymbolTable,
                        register, isMain,offset,fp_offset,labelMatch,gbc);
                mipsFunc.addMipsBasicBlock(mipsBasicBlockBuilder.generateMipsBasicBlock(strings));
                offset = mipsBasicBlockBuilder.getOffset();
                fp_offset = mipsBasicBlockBuilder.getFpOffset();
            }
        }
        return mipsFunc;
    }
}

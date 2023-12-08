package Middle.Value.Func;

import Lexer.TokenType;
import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.*;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.BasicBlock.BasicBlockBuilder;
import Middle.Value.BasicBlock.BasicBlockCnt;
import Middle.Value.Instruction.AllInstructions.Label;
import Middle.Value.Instruction.AllInstructions.Ret;
import SymbolTable.SymbolTable;
import SyntaxTree.FuncDefNode;
import SyntaxTree.FuncFParamNode;
import SymbolTable.Symbol;
import SymbolTable.SymbolType;
import SyntaxTree.FuncFParamsNode;
import SyntaxTree.MainFuncDefNode;

import java.util.ArrayList;

public class FuncBuilder {
    private FuncDefNode funcDefNode;

    private SymbolTable symbolTable;

    private MainFuncDefNode mainFuncDefNode;

    private BasicBlockCnt basicBlockCnt;

    public FuncBuilder (FuncDefNode funcDefNode, SymbolTable symbolTable, BasicBlockCnt basicBlockCnt) {
        this.funcDefNode = funcDefNode;
        this.symbolTable = symbolTable;
        this.basicBlockCnt = basicBlockCnt;
    }

    public FuncBuilder(MainFuncDefNode mainFuncDefNode,SymbolTable symbolTable,BasicBlockCnt basicBlockCnt) {
        this.mainFuncDefNode = mainFuncDefNode;
        this.symbolTable = symbolTable;
        this.basicBlockCnt = basicBlockCnt;
    }

    public Func generateFunc() {
        ValueType type;
        ValueType ret;
        FuncCnt funcCnt = new FuncCnt();
        if (funcDefNode.getFuncType().getTokenType() == TokenType.INTTK) {
            ret = new IntType(32);
        } else {
            ret = new VoidType();
        }
        createNewTable();
        ArrayList<LlvmIrValue> paramsLlvmIrType = new ArrayList<>();
        ArrayList<Symbol> symbols = new ArrayList<>();
        if (funcDefNode.getFuncFParamsNode() != null) {
            ArrayList<FuncFParamNode> params = funcDefNode.getFuncFParamsNode().getFuncFParamNodes();
            for (FuncFParamNode funcFParamNode : params) {
                paramsLlvmIrType.add(generateParamsType(funcFParamNode,funcCnt.getCnt(),symbols));
            }
        }
        //Block
        SymbolTable temp = symbolTable;
        symbolTable = symbolTable.getParent();
        Symbol symbol = new Symbol(funcDefNode.getIdent(),SymbolType.FUNC);
        symbol.setParams(symbols);
        symbolTable.addItem(symbol);
        type = new FuncType(ret,paramsLlvmIrType);
        Func func = new Func("@"+funcDefNode.getIdent().getToken(),type);
        symbol.setLlvmIrValue(func);
        BasicBlockBuilder basicBlockBuilder = new BasicBlockBuilder(funcCnt,basicBlockCnt);
        int num = basicBlockCnt.getCnt(); //第一个语句块
        BasicBlock basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
        basicBlock.addOneInstruction(new Label(String.valueOf(num),new LabelType(num)));
        ArrayList<BasicBlock> tbs = basicBlockBuilder.generateInitBasicBlocks(funcDefNode.getBlockNode(),symbol,basicBlock,temp);
        if (symbol.getRetype() == 0) {
            BasicBlock bb = tbs.get(tbs.size()-1);
            Ret r = new Ret("",null,null);
            bb.addOneInstruction(r);
        }
        func.addBasicBlocks(tbs);
        return func;
    }

    private LlvmIrValue generateParamsType(FuncFParamNode funcFParamNode,int cnt,ArrayList<Symbol> symbols) {
        int dim = funcFParamNode.getLParentNum();
        LlvmIrValue llvmIrValue = null;
        Symbol symbol = null;
        if (dim == 0) {
            String name = "%v_" + cnt;
            llvmIrValue = new LlvmIrValue(name,new IntType(32));
            llvmIrValue.setIsParam(true);
            symbol = new Symbol(funcFParamNode.getToken(),SymbolType.VAR);
            symbol.setDim(0);
            symbol.setLlvmIrValue(llvmIrValue);
        } else if (dim == 1) {
            //code_generation_2
            String name = "%v_" + cnt;
            llvmIrValue = new LlvmIrValue(name,new PointerType(new ArrayType(1,-1,-1)));
            llvmIrValue.setIsParam(true);
            llvmIrValue.setDim(1);
            symbol = new Symbol(funcFParamNode.getToken(),SymbolType.VAR_ARRAY1);
            symbol.setDim(1);
            symbol.setLlvmIrValue(llvmIrValue);
        } else if (dim == 2) {
            //code_generation_2
            String name = "%v_" + cnt;
            int column = funcFParamNode.getConstExpNodes().get(0).calcuateValue(symbolTable);
            llvmIrValue = new LlvmIrValue(name,new PointerType(new ArrayType(2,-1,column)));
            llvmIrValue.setIsParam(true);
            symbol = new Symbol(funcFParamNode.getToken(),SymbolType.VAR_ARRAY2);
            symbol.setDim(2);
            llvmIrValue.setDim(2);
            symbol.setLRNumber(0,column);
            symbol.setLlvmIrValue(llvmIrValue);
            llvmIrValue.setColumn(column);
        } else {
            System.out.println("error");
        }
        symbolTable.addItem(symbol);
        symbols.add(symbol);
        return llvmIrValue;
    }

    public Func generateMainFun() {
        ValueType type;
        ValueType ret = new IntType(32);
        FuncCnt funcCnt = new FuncCnt();
        ArrayList<LlvmIrValue> paramsValueType = new ArrayList<>();
        ArrayList<Symbol> params = new ArrayList<>();
        Symbol symbol = new Symbol(mainFuncDefNode.getMain(),SymbolType.FUNC);
        type = new FuncType(ret,paramsValueType);
        symbol.setParams(params);
        Func func = new Func("@" + mainFuncDefNode.getMain().getToken(),type);
        symbol.setLlvmIrValue(func);
        symbolTable.addItem(symbol);
        createNewTable();
        BasicBlockBuilder basicBlockBuilder = new BasicBlockBuilder(funcCnt,basicBlockCnt);
        int num = basicBlockCnt.getCnt();
        BasicBlock basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
        basicBlock.addOneInstruction(new Label(String.valueOf(num),new LabelType(num)));
        func.addBasicBlocks(basicBlockBuilder.generateInitBasicBlocks(mainFuncDefNode.getBlockNode(),symbol,basicBlock,symbolTable));
        symbolTable = symbolTable.getParent();
        return func;
    }

    private void createNewTable() {
        SymbolTable curSymbolTable = new SymbolTable(symbolTable);
        symbolTable.addChild(curSymbolTable);
        symbolTable = curSymbolTable;
    }
}

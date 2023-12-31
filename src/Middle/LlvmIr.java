package Middle;

import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.BasicBlock.BasicBlockCnt;
import Middle.Value.Func.Func;
import Middle.Value.Func.FuncBuilder;
import Middle.Value.Func.FuncCnt;
import Middle.Value.GlobalVar.GlobalVar;
import Middle.Value.GlobalVar.GlobalVarBuilder;
import SymbolTable.SymbolTable;
import SyntaxTree.CompUnitNode;
import SyntaxTree.DeclNode;
import SyntaxTree.FuncDefNode;
import SyntaxTree.MainFuncDefNode;

import java.util.ArrayList;

public class LlvmIr {
    private CompUnitNode compUnitNode;
    private LlvmIrModule llvmIrModule;
    private SymbolTable symbolTable;
    private BasicBlockCnt basicBlockCnt;
    private FuncCnt funcCnt;

    public LlvmIr(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
        this.llvmIrModule = new LlvmIrModule();
        this.symbolTable = new SymbolTable(null);
        this.basicBlockCnt = new BasicBlockCnt();
        this.funcCnt = new FuncCnt();
    }

    public LlvmIrModule generateIrModule() {
        for (DeclNode declNode:compUnitNode.getDeclNodes()) {
            GlobalVarBuilder globalVarBuilder = new GlobalVarBuilder(declNode,symbolTable);
            ArrayList<GlobalVar> globalVars = globalVarBuilder.generateGlobalVar();
            for (GlobalVar globalVar : globalVars) {
                llvmIrModule.addGlobalVar(globalVar);
            }
        }
        //BasicBlockCnt bbc = new BasicBlockCnt();
        for (FuncDefNode funcDefNode:compUnitNode.getFuncDefNodes()) {
            FuncBuilder funcBuilder = new FuncBuilder(funcDefNode,symbolTable,basicBlockCnt,funcCnt);
            Func func = funcBuilder.generateFunc();
            llvmIrModule.addFunc(func);
        }
        MainFuncDefNode mainFuncDefNode = compUnitNode.getMainFuncDefNode();
        FuncBuilder funcBuilder = new FuncBuilder(mainFuncDefNode,symbolTable,basicBlockCnt,funcCnt);
        Func func = funcBuilder.generateMainFun();
        llvmIrModule.addFunc(func);
        return llvmIrModule;
    }

    public BasicBlockCnt getBasicBlockCnt() {
        return basicBlockCnt;
    }
}

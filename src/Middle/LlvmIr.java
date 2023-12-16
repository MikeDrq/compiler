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

    public LlvmIr(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
        this.llvmIrModule = new LlvmIrModule();
        this.symbolTable = new SymbolTable(null);
        this.basicBlockCnt = new BasicBlockCnt();
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
        FuncCnt funcCnt = new FuncCnt();
        for (FuncDefNode funcDefNode:compUnitNode.getFuncDefNodes()) {
            FuncBuilder funcBuilder = new FuncBuilder(funcDefNode,symbolTable,basicBlockCnt);
            Func func = funcBuilder.generateFunc(funcCnt);
            llvmIrModule.addFunc(func);
        }
        MainFuncDefNode mainFuncDefNode = compUnitNode.getMainFuncDefNode();
        FuncBuilder funcBuilder = new FuncBuilder(mainFuncDefNode,symbolTable,basicBlockCnt);
        Func func = funcBuilder.generateMainFun(funcCnt);
        llvmIrModule.addFunc(func);
        return llvmIrModule;
    }

    public BasicBlockCnt getBasicBlockCnt() {
        return basicBlockCnt;
    }
}

package Middle;

import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Func.FuncBuilder;
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

    public LlvmIr(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
        this.llvmIrModule = new LlvmIrModule();
        this.symbolTable = new SymbolTable(null);
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
            FuncBuilder funcBuilder = new FuncBuilder(funcDefNode,symbolTable);
            Func func = funcBuilder.generateFunc();
            llvmIrModule.addFunc(func);
        }
        MainFuncDefNode mainFuncDefNode = compUnitNode.getMainFuncDefNode();
        FuncBuilder funcBuilder = new FuncBuilder(mainFuncDefNode,symbolTable);
        Func func = funcBuilder.generateMainFun();
        llvmIrModule.addFunc(func);
        return llvmIrModule;
    }
}

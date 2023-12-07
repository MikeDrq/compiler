package Middle.Value.Instruction;

import Lexer.Token;
import Lexer.TokenType;
import Middle.LlvmIrModule;
import Middle.LlvmIrValue;
import Middle.Type.*;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Func.Func;
import Middle.Value.Func.FuncCnt;
import Middle.Value.Instruction.AllInstructions.*;
import SymbolTable.SymbolTable;
import SyntaxTree.*;
import SymbolTable.Symbol;
import SymbolTable.SymbolType;

import java.util.ArrayList;
import java.util.LinkedList;

public class InstructionBuilder {
    private DeclNode declNode;
    private StmtNode stmtNode;
    private BasicBlock basicBlock;
    private SymbolTable symbolTable;
    private ArrayList<Instruction> instructions;
    private FuncCnt funcCnt;
    private AddExpNode addExpForRel;
    private ForStmtNode forStmtNode;
    private LValNode lValNode;
    private ExpNode expNode;

    public InstructionBuilder(DeclNode declNode, BasicBlock basicBlock, SymbolTable symbolTable, FuncCnt funcCnt) {
        this.declNode = declNode;
        this.basicBlock = basicBlock;
        this.symbolTable = symbolTable;
        this.instructions = new ArrayList<>();
        this.funcCnt = funcCnt;
    }

    public InstructionBuilder(StmtNode stmtNode,BasicBlock basicBlock,SymbolTable symbolTable,FuncCnt funcCnt) {
        this.stmtNode = stmtNode;
        this.basicBlock = basicBlock;
        this.symbolTable = symbolTable;
        this.instructions = new ArrayList<>();
        this.funcCnt = funcCnt;
    }

    public InstructionBuilder(SymbolTable symbolTable,FuncCnt funcCnt,AddExpNode addExpNode) {
        this.symbolTable = symbolTable;
        this.funcCnt = funcCnt;
        this.addExpForRel = addExpNode;
        this.instructions = new ArrayList<>();
    }

    public InstructionBuilder(SymbolTable symbolTable,FuncCnt funcCnt,ForStmtNode forStmtNode) {
        this.symbolTable = symbolTable;
        this.funcCnt = funcCnt;
        this.forStmtNode = forStmtNode;
        this.instructions = new ArrayList<>();
    }

    public InstructionBuilder(SymbolTable symbolTable,FuncCnt funcCnt,ExpNode expNode) {
        this.symbolTable = symbolTable;
        this.funcCnt = funcCnt;
        this.expNode = expNode;
        this.instructions = new ArrayList<>();
    }

    public ArrayList<Instruction> generateInstruction() {
        if (declNode != null) {
            if (declNode.queryConst()) {
                generateConstInstr(declNode.getConstDeclNode());
            } else {
                generateVarInstr(declNode.getVarDeclNode());
            }
        } else {
            if (stmtNode.getMark() == 1) {
                generateAssignExpInstr(stmtNode.getlValNode(),stmtNode.getExpNode());
            } else if (stmtNode.getMark() == 2) {
                generateExpInstr(stmtNode.getExpNode());
            } else if (stmtNode.getMark() == 10) {
                if (stmtNode.getWordGetint() == null ) {
                    System.out.println("error");
                }
                generateGetIntInstr(stmtNode.getlValNode());
            } else if (stmtNode.getMark() == 11) {
                generatePrintInstr(stmtNode.getFormatstring(),stmtNode.getExpNodes());
            } else {
                //
            }
        }
        return instructions;
    }

    public LlvmIrValue generateSpecialInstructions(BasicBlock bb) {
        LlvmIrValue ret = dealAddExpNodes(this.addExpForRel);
        bb.addInstructions(instructions);
        return ret;
    }

    public void generateForInstructions(BasicBlock bb) {
        generateAssignExpInstr(forStmtNode.getlValNode(),forStmtNode.getExpNode());
        bb.addInstructions(instructions);
    }
    /*
    1: LVal '=' Exp ';'
    2: [Exp] ';'
    3: ';'
    4: Block
    5: 'if'
    6: 'for'
    7: 'break'
    8: 'continue'
    9: 'return'
    10: LVal '=' 'getint''('')'';'
    11: 'printf''('FormatString{','Exp}')'';'
     */

    public void generateConstInstr(ConstDeclNode constDeclNode) {
        ArrayList<ConstDefNode> constDefNodes = constDeclNode.getConstDefNodes();
        for (ConstDefNode constDefNode : constDefNodes) {
            int dim = constDefNode.getLBrackNum();
            Token ident = constDefNode.getIdent();
            if (dim == 0) {
                Symbol symbol = new Symbol(ident, SymbolType.CONST);
                symbol.setDim(0);
                symbolTable.addItem(symbol);
                //String name = "%v_" + funcCnt.getCnt();
                int num = constDefNode.getConstInitValNode().getConstExpNode().calcuateValue(symbolTable);
                symbol.setInitial(num);
                LlvmIrValue llvmIrValue = new LlvmIrValue(String.valueOf(num),new IntType(32));
                /*Alloca alloca = new Alloca(new IntType(32),name,llvmIrValue);
                instructions.add(alloca);
                setConstInit(symbol,constDefNode.getConstInitValNode(),dim,alloca);*/
                symbol.setLlvmIrValue(llvmIrValue);
            } else if (dim == 1) {
                //code_generation_2
                Symbol symbol = new Symbol(ident,SymbolType.CONST_ARRAY1);
                symbol.setDim(1);
                symbolTable.addItem(symbol);
                ArrayList<ConstExpNode> constExpNodes = constDefNode.getConstExpNodes();
                int num = constExpNodes.get(0).calcuateValue(symbolTable);
                String name = "%v_" + funcCnt.getCnt();
                LlvmIrValue llvmIrValue = new LlvmIrValue(name,new ArrayType(1,0,num));
                llvmIrValue.setDim(1);
                llvmIrValue.setColumn(num);
                symbol.setLRNumber(0,num);
                Alloca alloca = new Alloca(new ArrayType(1,0,num),name,llvmIrValue);
                instructions.add(alloca);
                setConstInit(symbol,constDefNode.getConstInitValNode(),dim,alloca);
                symbol.setLlvmIrValue(llvmIrValue);
            } else if (dim == 2) {
                //code_generation_2
                Symbol symbol = new Symbol(ident,SymbolType.CONST_ARRAY2);
                symbol.setDim(2);
                symbolTable.addItem(symbol);
                ArrayList<ConstExpNode> constExpNodes = constDefNode.getConstExpNodes();
                int row = constExpNodes.get(0).calcuateValue(symbolTable);
                int column = constExpNodes.get(1).calcuateValue(symbolTable);
                String name = "%v_" + funcCnt.getCnt();
                LlvmIrValue llvmIrValue = new LlvmIrValue(name,new ArrayType(2,row,column));
                llvmIrValue.setDim(2);
                llvmIrValue.setRaw(row);
                llvmIrValue.setColumn(column);
                symbol.setLRNumber(row,column);
                Alloca alloca = new Alloca(new ArrayType(2,row,column),name,llvmIrValue);
                instructions.add(alloca);
                setConstInit(symbol,constDefNode.getConstInitValNode(),dim,alloca);
                symbol.setLlvmIrValue(llvmIrValue);
            } else {
                System.out.println("error");
            }
        }

    }

    public void generateVarInstr(VarDeclNode varDeclNode) {
        ArrayList<VarDefNode> varDefNodes = varDeclNode.getVarDefNodes();
        for (VarDefNode varDefNode : varDefNodes) {
            int dim = varDefNode.getLBrackNum();
            Token ident = varDefNode.getIdent();
            if (dim == 0) {
                Symbol symbol = new Symbol(ident, SymbolType.VAR);
                symbol.setDim(0);
                symbolTable.addItem(symbol);
                String name = "%v_" + funcCnt.getCnt();
                LlvmIrValue llvmIrValue = new LlvmIrValue(name,new IntType(32));
                symbol.setLlvmIrValue(llvmIrValue);
                Alloca alloca = new Alloca(new IntType(32),name,llvmIrValue);
                instructions.add(alloca);
                if (varDefNode.getInitValNode() != null) {
                    setVarInt(symbol,varDefNode.getInitValNode(),dim,alloca);
                }
            } else if (dim == 1) {
                //code_generation_2
                Symbol symbol = new Symbol(ident,SymbolType.VAR_ARRAY1);
                symbol.setDim(1);
                symbolTable.addItem(symbol);
                String name = "%v_" + funcCnt.getCnt();
                int column = varDefNode.getConstExpNodes().get(0).calcuateValue(symbolTable);
                LlvmIrValue llvmIrValue = new LlvmIrValue(name,new ArrayType(1,0,column));
                llvmIrValue.setDim(1);
                llvmIrValue.setColumn(column);
                symbol.setLRNumber(0,column);
                symbol.setLlvmIrValue(llvmIrValue);
                Alloca alloca = new Alloca(new ArrayType(1,0,column),name,llvmIrValue);
                instructions.add(alloca);
                if (varDefNode.getInitValNode() != null) {
                    setVarInt(symbol,varDefNode.getInitValNode(),1,alloca);
                }
            } else if (dim == 2) {
                //code_generation_2
                Symbol symbol = new Symbol(ident,SymbolType.VAR_ARRAY1);
                symbol.setDim(2);
                symbolTable.addItem(symbol);
                String name = "%v_" + funcCnt.getCnt();
                int row = varDefNode.getConstExpNodes().get(0).calcuateValue(symbolTable);
                int column = varDefNode.getConstExpNodes().get(1).calcuateValue(symbolTable);
                LlvmIrValue llvmIrValue = new LlvmIrValue(name,new ArrayType(2,row,column));
                llvmIrValue.setDim(2);
                symbol.setLlvmIrValue(llvmIrValue);
                llvmIrValue.setRaw(row);
                llvmIrValue.setColumn(column);
                symbol.setLRNumber(row,column);
                Alloca alloca = new Alloca(new ArrayType(2,row,column),name,llvmIrValue);
                instructions.add(alloca);
                if (varDefNode.getInitValNode() != null) {
                    setVarInt(symbol,varDefNode.getInitValNode(),2,alloca);
                }
            } else {
                System.out.println("error");
            }
        }
    }

    public void setConstInit(Symbol symbol,ConstInitValNode constInitValNode,int dim,Alloca alloca) {
        if (dim == 0) {
            ConstExpNode constExpNode = constInitValNode.getConstExpNode();
            LlvmIrValue llvmIrValue = dealAddExpNodes(constExpNode.getAddExpNode());
            Store store = new Store("store",null,llvmIrValue,alloca);
            instructions.add(store);
        } else if (dim == 1) {
            //code_generation_2
            ArrayList<ConstInitValNode> constInitValNodes = constInitValNode.getConstInitValNodes();
            int cnt = 0;
            for (ConstInitValNode civ : constInitValNodes) {
                String name = "%v_" + funcCnt.getCnt();
                Getelementptr getelementptr = new Getelementptr(name,alloca.getType(),alloca,1,symbol.getArray_column(),cnt);
                instructions.add(getelementptr);
                LlvmIrValue llvmIrValue = dealAddExpNodes(civ.getConstExpNode().getAddExpNode());
                Store store = new Store("store",null,llvmIrValue,getelementptr);
                instructions.add(store);
                cnt++;
            }
            symbol.setAllZero(false);
        } else if (dim == 2) {
            //code_generation_2
            ArrayList<ConstInitValNode> constInitValNodes = constInitValNode.getConstInitValNodes();
            int first = 0;
            for (ConstInitValNode civ : constInitValNodes) {
                ArrayList<ConstInitValNode> inCiv = civ.getConstInitValNodes();
                int second = 0;
                for (ConstInitValNode simple_civ : inCiv) {
                    String name = "%v_" + funcCnt.getCnt();
                    Getelementptr getelementptr = new Getelementptr(name,alloca.getType(),
                            alloca,2,symbol.getArray_row(),symbol.getArray_column(),first,second);
                    instructions.add(getelementptr);
                    LlvmIrValue llvmIrValue = dealAddExpNodes(simple_civ.getConstExpNode().getAddExpNode());
                    second++;
                    Store store = new Store("store",null,llvmIrValue,getelementptr);
                    instructions.add(store);
                }first++;
            }
        } else {
            System.out.println("error");
        }
    }

    public void setVarInt(Symbol symbol,InitValNode initValNode,int dim,Alloca alloca) {
        if (dim == 0) {
            ExpNode expNode = initValNode.getExpNode();
            LlvmIrValue llvmIrValue = dealExpNode(expNode);
            Store store = new Store("store",null,llvmIrValue,alloca);
            instructions.add(store);
        } else if (dim == 1) {
            //code_generation_2
            ArrayList<InitValNode> initValNodes = initValNode.getInitValNodes();
            int cnt = 0;
            for (InitValNode ivn : initValNodes) {
                String name = "%v_" + funcCnt.getCnt();
                Getelementptr getelementptr = new Getelementptr(name,alloca.getType(),alloca,1,symbol.getArray_column(),cnt);
                instructions.add(getelementptr);
                LlvmIrValue llvmIrValue =  dealExpNode(ivn.getExpNode());
                Store store = new Store("store",null,llvmIrValue,getelementptr);
                instructions.add(store);
                cnt++;
            }
        } else if (dim == 2) {
            //code_generation_2
            int row = 0;
            ArrayList<InitValNode> initValNodes = initValNode.getInitValNodes();
            for (InitValNode ivn : initValNodes) {
                int column = 0;
                ArrayList<InitValNode> iivns = ivn.getInitValNodes();
                for (InitValNode iivn : iivns) {
                    String name = "%v_" + funcCnt.getCnt();
                    Getelementptr getelementptr = new Getelementptr(name,alloca.getType(),alloca,2,symbol.getArray_row(),symbol.getArray_column(),row,column);
                    instructions.add(getelementptr);
                    LlvmIrValue llvmIrValue = dealExpNode(iivn.getExpNode());
                    Store store = new Store("store",null,llvmIrValue,getelementptr);
                    instructions.add(store);
                    column++;
                }
                row++;
            }

        } else {
            System.out.println("error");
        }
    }

    public LlvmIrValue dealExpNode(ExpNode expNode) {
        AddExpNode addExpNodes = expNode.getAddExpNode();
        LlvmIrValue llvmIrValue = dealAddExpNodes(addExpNodes);
        return llvmIrValue;
    }

    public LlvmIrValue dealAddExpNodes(AddExpNode addExpNode) {
        ArrayList<Token> op = addExpNode.getOp();
        ArrayList<MulExpNode> mulExpNodes = addExpNode.getMulExpNodes();
        LlvmIrValue leftValue = dealMulExpNodes(mulExpNodes.get(0));
        for (int i = 0;i < op.size();i++) {
            InstructionType instructionType = null;
            if (op.get(i).getTokenType() == TokenType.PLUS) {
                instructionType = InstructionType.add;
            } else if (op.get(i).getTokenType() == TokenType.MINU) {
                instructionType = InstructionType.sub;
            } else {
                System.out.println("error");
            }
            LlvmIrValue rightValue = dealMulExpNodes(mulExpNodes.get(i+1));
            String name = "%v_" + funcCnt.getCnt();
            Calculate calculate = new Calculate(name,leftValue,rightValue,instructionType,new IntType(32));
            instructions.add(calculate);
            leftValue = calculate;
        }
        return leftValue;
    }

    public LlvmIrValue dealMulExpNodes(MulExpNode mulExpNode) {
        ArrayList<Token> op = mulExpNode.getOp();
        ArrayList<UnaryExpNode> unaryExpNodes = mulExpNode.getUnaryExpNodes();
        LlvmIrValue leftValue = dealUnaryExpNode(unaryExpNodes.get(0));
        for (int i = 0;i < op.size();i++) {
            InstructionType instructionType = null;
            if (op.get(i).getTokenType() == TokenType.DIV) {
                instructionType = InstructionType.sdiv;
            } else if (op.get(i).getTokenType() == TokenType.MULT) {
                instructionType = InstructionType.mul;
            } else if (op.get(i).getTokenType() == TokenType.MOD) {
                instructionType = InstructionType.srem;
            }
            LlvmIrValue rightValue = dealUnaryExpNode(unaryExpNodes.get(i+1));
            String name = "%v_" + funcCnt.getCnt();
            Calculate calculate = new Calculate(name,leftValue,rightValue,instructionType,new IntType(32));
            instructions.add(calculate);
            leftValue = calculate;
        }
        return leftValue;
    }

    public LlvmIrValue dealUnaryExpNode(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.getPrimaryExpNode() != null) {
            return dealPrimaryExpNode(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getIdent() != null) {
            return dealCallFunc(unaryExpNode.getIdent(),unaryExpNode.getFuncRParamsNodes());
        } else {
            Token op = unaryExpNode.getUnaryOpNode().getUnaryOpNode();
            LlvmIrValue llvmIrValue = dealUnaryExpNode(unaryExpNode.getUnaryExpNode());
            if(op.getTokenType() == TokenType.PLUS) {
                return llvmIrValue; //+可省略
            } else if (op.getTokenType() == TokenType.MINU) {
                String name = "%v_" + funcCnt.getCnt();
                InstructionType instructionType = InstructionType.sub;
                Calculate calculate = new Calculate(name,new LlvmIrValue("0",new IntType(32)),
                        llvmIrValue,instructionType,new IntType(32));
                instructions.add(calculate);
                return calculate;
            } else if (op.getTokenType() == TokenType.NOT) {
                //code_generation_2
                String name = "%v_" + funcCnt.getCnt();
                InstructionType instructionType = InstructionType.xor;
                /*Calculate calculate = new Calculate(name,new LlvmIrValue("-1",new IntType(32)),
                        llvmIrValue,instructionType,new IntType(32));*/
                String cond = "eq";
                Calculate calculate = new Calculate(name,llvmIrValue,new LlvmIrValue("0",new IntType(32)),
                        InstructionType.icmp,new IntType(1),cond);
                instructions.add(calculate);
                name = "%v_" + funcCnt.getCnt();
                Zext zext = new Zext(name,null,calculate);
                instructions.add(zext);
                return zext;
            } else {
                System.out.println("error");
            }
        }
        System.out.println("error");
        return null;
    }

    public LlvmIrValue dealLValNode(LValNode lValNode) {
        Symbol symbol = symbolTable.getVar(lValNode.getIdent());
        int dim = symbol.getDim();
        if (dim == 0) {
            LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
            if (!(llvmIrValue.getName().charAt(0) == '@' || llvmIrValue.getName().charAt(0) == '%')) {
                return llvmIrValue;
            }
            if (llvmIrValue.isParam()) {
                return llvmIrValue;
            }
            String name = "%v_" + funcCnt.getCnt();
            Load load = new Load(name,new IntType(32),llvmIrValue);
            this.instructions.add(load);
            return load;
        } else if (dim == 1) {
            LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
            int c = symbol.getArray_column();
            if (lValNode.getExpNodes().size() == 0) { //其为实参，一维数组传递
                if (llvmIrValue.getType() instanceof PointerType) {
                    llvmIrValue = loadArray(llvmIrValue);
                }
                String name_offset = "%v_" + funcCnt.getCnt();
                Getelementptr getelementptr = new Getelementptr(name_offset, llvmIrValue.getType(), llvmIrValue, 1, c,0);
                instructions.add(getelementptr);
                getelementptr.setRParamDim(1);
                return getelementptr;
            } else {
                if (llvmIrValue.isParam()) {
                    return llvmIrValue;
                } else {
                    if (llvmIrValue.getType() instanceof PointerType) {
                        llvmIrValue = loadArray(llvmIrValue);
                    }
                    ExpNode expNode = lValNode.getExpNodes().get(0);
                    LlvmIrValue posValue = dealExpNode(expNode);
                    String name_offset = "%v_" + funcCnt.getCnt();
                    Getelementptr getelementptr = new Getelementptr(name_offset, llvmIrValue.getType(), llvmIrValue, 1, c, posValue);
                    instructions.add(getelementptr);
                    String name = "%v_" + funcCnt.getCnt();
                    Load load = new Load(name, new IntType(32), getelementptr);
                    instructions.add(load);
                    load.setRParamDim(0);
                    load.setDim(1);
                    return load;
                }
            }
        } else if (dim == 2) {
            LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
            int r = symbol.getArray_row();
            int c = symbol.getArray_column();
            if (lValNode.getExpNodes().size() == 0) {
                if (llvmIrValue.getType() instanceof PointerType) {
                    llvmIrValue = loadArray(llvmIrValue);
                }
                String name_offset = "%v_" + funcCnt.getCnt();
                Getelementptr getelementptr = new Getelementptr(name_offset,llvmIrValue.getType(), llvmIrValue,
                        2, r, c, 0,0);
                instructions.add(getelementptr);
                getelementptr.setRParamDim(2);
                return  getelementptr;
            } else if (lValNode.getExpNodes().size() == 1) {
                if (llvmIrValue.getType() instanceof PointerType) {
                    llvmIrValue = loadArray(llvmIrValue);
                }
                ExpNode expNode1 = lValNode.getExpNodes().get(0);
                LlvmIrValue pos1 = dealExpNode(expNode1);
                String name_offset = "%v_" + funcCnt.getCnt();
                Getelementptr getelementptr = new Getelementptr(name_offset, llvmIrValue.getType(), llvmIrValue,
                        2, r, c, pos1, new LlvmIrValue("0",new IntType(32)));
                instructions.add(getelementptr);
                getelementptr.setRParamDim(1);
                return  getelementptr;
            } else {
                if (llvmIrValue.isParam()) {
                    return llvmIrValue; //如果是形参直接返回即可
                } else {
                    if (llvmIrValue.getType() instanceof PointerType) {
                        llvmIrValue = loadArray(llvmIrValue); //如果是指针类型，需要先load地址
                    }
                    ExpNode expNode1 = lValNode.getExpNodes().get(0);
                    ExpNode expNode2 = lValNode.getExpNodes().get(1);
                    LlvmIrValue pos1 = dealExpNode(expNode1);
                    LlvmIrValue pos2 = dealExpNode(expNode2);
                    String name_offset = "%v_" + funcCnt.getCnt();
                    Getelementptr getelementptr = new Getelementptr(name_offset, llvmIrValue.getType(), llvmIrValue,
                            2, r, c, pos1, pos2);
                    instructions.add(getelementptr);
                    String name = "%v_" + funcCnt.getCnt();
                    Load load = new Load(name, new IntType(32), getelementptr);
                    instructions.add(load);
                    load.setRParamDim(0);
                    load.setDim(2);
                    return load;
                }
            }
        } else {
            System.out.println("error");
        }
        return null;
    }

    public LlvmIrValue dealPrimaryExpNode (PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getlValNode() != null) {
            LValNode lValNode = primaryExpNode.getlValNode();
            return dealLValNode(lValNode);
        } else if (primaryExpNode.getNumberNode() != null) {
            return new LlvmIrValue(String.valueOf(primaryExpNode.getNumberNode().getNumber().getToken()),new IntType(32));
        } else {
            return dealExpNode(primaryExpNode.getExpNode());
        }
    }

    public LlvmIrValue dealCallFunc (Token ident,FuncRParamsNode funcRParamsNode) {
        Symbol symbol = symbolTable.getVar(ident);
        LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
        ArrayList<ExpNode> params = new ArrayList<>();
        if (funcRParamsNode != null) {
            params = funcRParamsNode.getExpNodes();
        }
        ArrayList<LlvmIrValue> values = new ArrayList<>();
        for (ExpNode expNode : params) {
            LlvmIrValue value = dealExpNode(expNode);
            values.add(value);
        }
        String name = "";
        FuncType funcType = (FuncType) llvmIrValue.getType();
        if (funcType.getRetType() instanceof IntType) {
            name = "%v_" + funcCnt.getCnt();
        }
        Call call = new Call(name,llvmIrValue.getType(),values,llvmIrValue);
        instructions.add(call);
        return call;
    }

    public void generateAssignExpInstr(LValNode lValNode,ExpNode expNode) {
        Symbol symbol = symbolTable.getVar(lValNode.getIdent());
        LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
        int dim = symbol.getDim();
        if (dim == 0) {
             LlvmIrValue left = dealExpNode(expNode);
             Store store = new Store("store",null,left,llvmIrValue);
             instructions.add(store);
        } else if (dim == 1) {
            //code_generation_2
            LlvmIrValue left = dealExpNode(expNode);
            LlvmIrValue pos = dealExpNode(lValNode.getExpNodes().get(0));
            //先取出数组的位置
            if (llvmIrValue.getType() instanceof PointerType) {
                llvmIrValue = loadArray(llvmIrValue);
            }
            String name = "%v_" + funcCnt.getCnt();
            Getelementptr getelementptr = new Getelementptr(name,llvmIrValue.getType(),llvmIrValue,dim,symbol.getArray_column(),pos);
            instructions.add(getelementptr);
            Store store = new Store("store",null,left,getelementptr);
            instructions.add(store);
        } else if (dim == 2) {
            //code_generation_2
            LlvmIrValue left = dealExpNode(expNode);
            LlvmIrValue row = dealExpNode(lValNode.getExpNodes().get(0));
            LlvmIrValue column = dealExpNode(lValNode.getExpNodes().get(1));
            if (llvmIrValue.getType() instanceof PointerType) {
                llvmIrValue = loadArray(llvmIrValue);
            }
            String name = "%v_" + funcCnt.getCnt();
            Getelementptr getelementptr = new Getelementptr(name,llvmIrValue.getType(),llvmIrValue,dim,symbol.getArray_row(),symbol.getArray_column(),row,column);
            instructions.add(getelementptr);
            Store store = new Store("store",null,left,getelementptr);
            instructions.add(store);
        } else {
            System.out.println("error");
        }
    }

    public LlvmIrValue loadArray(LlvmIrValue llvmIrValue) {
        String name = "%v_" + funcCnt.getCnt();
        Load load = new Load(name,llvmIrValue.getType(),llvmIrValue);
        instructions.add(load);
        return load;
    }

    public void generateExpInstr(ExpNode expNode) {
        dealExpNode(expNode);
    }

    public ArrayList<Instruction> generateReturnInstr() {
        Ret ret;
        if (this.expNode != null) {
            LlvmIrValue right = dealExpNode(this.expNode);
            ret = new Ret("",null,right);
        } else {
            ret = new Ret("",null,null);
        }
        instructions.add(ret);
        return instructions;
    }

    public void generateGetIntInstr(LValNode lValNode) {
        Symbol symbol = symbolTable.getVar(lValNode.getIdent());
        LlvmIrValue llvmIrValue = symbol.getLlvmIrValue();
        int dim = symbol.getDim();
        if (dim == 0) {
            String name = "%v_" + funcCnt.getCnt();
            Call call = new Call(name,new IntType(32),"@getint");
            instructions.add(call);
            Store store = new Store("store",null,call,llvmIrValue);
            instructions.add(store);
        } else if (dim == 1) {
            //code_generation_2
            LlvmIrValue pos = dealExpNode(lValNode.getExpNodes().get(0));
            if (llvmIrValue.getType() instanceof PointerType) {
                llvmIrValue = loadArray(llvmIrValue);
            }
            String name_pos = "%v_" + funcCnt.getCnt();
            Getelementptr getelementptr = new Getelementptr(name_pos,llvmIrValue.getType(),llvmIrValue,dim,symbol.getArray_column(),pos);
            instructions.add(getelementptr);
            String name = "%v_" + funcCnt.getCnt();
            Call call = new Call(name,new IntType(32),"@getint");
            instructions.add(call);
            Store store = new Store("store",null,call,getelementptr);
            instructions.add(store);
        } else if (dim == 2) {
            //code_generation_2
            LlvmIrValue row = dealExpNode(lValNode.getExpNodes().get(0));
            LlvmIrValue column = dealExpNode(lValNode.getExpNodes().get(1));
            if (llvmIrValue.getType() instanceof PointerType) {
                llvmIrValue = loadArray(llvmIrValue);
            }
            String name_pos = "%v_" + funcCnt.getCnt();
            Getelementptr getelementptr = new Getelementptr(name_pos,llvmIrValue.getType(),llvmIrValue,dim,symbol.getArray_row(),symbol.getArray_column(),row,column);
            instructions.add(getelementptr);
            String name_call = "%v_" + funcCnt.getCnt();
            Call call = new Call(name_call,new IntType(32),"@getint");
            instructions.add(call);
            Store store = new Store("store",null,call,getelementptr);
            instructions.add(store);
        } else {
            System.out.println("error");
        }
    }

    public void generatePrintInstr(Token formatString,ArrayList<ExpNode> expNodes) {
        ArrayList<LlvmIrValue> ops = new ArrayList<>();
        for (ExpNode expNode:expNodes) {
            LlvmIrValue right = dealExpNode(expNode);
            ops.add(right);
        }
        String s = formatString.getToken();
        s = s.substring(1,s.length() - 1);
        int cnt = 0;
        for (int i = 0;i < s.length();i++) {
            Call call;
            if (s.charAt(i) == '%') {
                call = new Call("",new VoidType(),"@putint",ops.get(cnt));
                cnt++;
                i++;
            } else if (s.charAt(i) == '\\') {
                if (i + 1 < s.length()) {
                    if (s.charAt(i+1) == 'n') {
                        call = new Call("",new VoidType(),"@putch",'\n');
                        i++;
                    } else {
                        call = new Call("",new VoidType(),"@putch",s.charAt(i));
                    }
                } else {
                    call = new Call("",new VoidType(),"@putch",s.charAt(i));
                }
            } else {
                call = new Call("",new VoidType(),"@putch",s.charAt(i));
            }
            instructions.add(call);
        }
    }

}

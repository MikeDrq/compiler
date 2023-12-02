package Middle.Value.BasicBlock;

import Lexer.Token;
import Middle.LlvmIrValue;
import Middle.Type.IntType;
import Middle.Type.LabelType;
import Middle.Value.Func.FuncCnt;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;
import Middle.Value.Instruction.InstructionBuilder;
import Middle.Value.Instruction.InstructionType;
import SymbolTable.SymbolTable;
import SyntaxTree.*;
import SymbolTable.Symbol;
import Lexer.TokenType;

import java.util.ArrayList;
import java.util.Iterator;

public class BasicBlockBuilder {
    private FuncCnt funcCnt;

    public BasicBlockBuilder (FuncCnt funcCnt) {
        this.funcCnt = funcCnt;
    }

    public boolean check(BlockItemNode blockItemNode) {
        if (blockItemNode.queryIsDeclNode()) {
            return true;
        } else {
            int mark = blockItemNode.getStmtNode().getMark();
            if ((mark >= 1 && mark <= 3) || (mark >= 10)) {
                return true;
            } else {
                return false;
            }
        }
    }
    public ArrayList<BasicBlock> generateInitBasicBlocks(BlockNode blockNode,Symbol symbol,BasicBlock basicBlock,SymbolTable symbolTable) {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
        ArrayList<BlockItemNode> blockItemNodes = blockNode.getBlockItemNodes();
        basicBlocks.add(basicBlock);
        if (symbol.getParamList().size() > 0) {
            dealParams(basicBlock,symbol.getParamList(),symbolTable);
        }
        ArrayList<BasicBlock> temp = generateBasicBlocks(basicBlock,blockItemNodes,symbolTable);
        for (BasicBlock b : temp) {
            basicBlocks.add(b);
        }
        return basicBlocks;
    }

    public ArrayList<BasicBlock> generateBasicBlocks(BasicBlock tbasicBlock,ArrayList<BlockItemNode> blockItemNodes,SymbolTable symbolTable) {
        BasicBlock basicBlock = tbasicBlock;
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
        int size = blockItemNodes.size();
        int pos = 0;
        while (pos < size) {
            if (check(blockItemNodes.get(pos))) {
                while (pos < size && check(blockItemNodes.get(pos))) {
                    if (blockItemNodes.get(pos).queryIsDeclNode()) {
                        generateDecl(basicBlock, blockItemNodes.get(pos).getDeclNode(),symbolTable);
                    } else {
                        BlockItemNode bn = blockItemNodes.get(pos);
                        int mark = bn.getStmtNode().getMark();
                        if (mark == 1) { //LVal '=' Exp ';'
                            generateStmt(basicBlock, bn.getStmtNode(), symbolTable);
                        } else if (mark == 2) { //[Exp] ';'
                            generateStmt(basicBlock, bn.getStmtNode(), symbolTable);
                        } else if (mark == 3) { //';'
                            pos++;
                            continue;
                        }  else if (mark == 10) { // LVal '=' 'getint''('')'';'
                            generateStmt(basicBlock, bn.getStmtNode(), symbolTable);
                        } else { //'printf''('FormatString{','Exp}')'';'
                            generateStmt(basicBlock, bn.getStmtNode(), symbolTable);
                        }
                    }
                    pos++;
                }
            } else {
                while (pos < size && !check(blockItemNodes.get(pos))) {
                    BlockItemNode bn = blockItemNodes.get(pos);
                    StmtNode s1 = bn.getStmtNode();
                    int mark = s1.getMark();
                    if (mark == 4) {
                        if (s1.getBlockNode().getBlockItemNodes().size() > 0) { //空块的话直接过
                            SymbolTable newSymbolTable = new SymbolTable(symbolTable);
                            symbolTable.addChild(newSymbolTable);
                            ArrayList<BasicBlock> bbs = generateBasicBlocks(basicBlock, s1.getBlockNode().getBlockItemNodes(), newSymbolTable);
                            for (BasicBlock bb : bbs) {
                                basicBlocks.add(bb);
                            }
                            if (basicBlocks.size() > 0) {
                                basicBlock = basicBlocks.get(basicBlocks.size() - 1);
                            }
                        }
                    } else if (mark == 5) { //'if'
                        //code_generation_2
                        ArrayList<BasicBlock> bbs = dealIf(basicBlock,symbolTable,s1);
                        basicBlock = bbs.get(bbs.size() - 1);//这里会改变basicBlock的值，他是 if 后的第一个语句块
                        for (BasicBlock bb : bbs) {
                            basicBlocks.add(bb);
                        }
                    } else if (mark == 6) { //'for'
                        //code_generation_2
                        ArrayList<BasicBlock> bbs = dealFor(basicBlock,symbolTable,s1);
                        basicBlock = bbs.get(bbs.size() - 1);
                        for (BasicBlock bb : bbs) {
                            basicBlocks.add(bb);
                        }
                    } else if (mark == 7) { //'break'
                        Br br = new Br("br",null);
                        basicBlock.addOneInstruction(br);
                        basicBlock.addABreak(br);
                        int num = -1;
                        basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
                        Label label = new Label(String.valueOf(num),new LabelType(num));
                        basicBlock.addOneInstruction(label);
                        basicBlocks.add(basicBlock);
                        return basicBlocks;
                        //code_generation_2
                    } else if (mark == 8) { //'continue'
                        //code_generation_2
                        Br br = new Br("br",null);
                        basicBlock.addOneInstruction(br);
                        basicBlock.addAContinue(br);
                        int num = -1;
                        basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
                        Label label = new Label(String.valueOf(num),new LabelType(num));
                        basicBlock.addOneInstruction(label);
                        basicBlocks.add(basicBlock);
                        return basicBlocks;
                    } else if (mark == 9) { //'return'
                        InstructionBuilder instructionBuilder = new InstructionBuilder(symbolTable,funcCnt,s1.getExpNode());
                        basicBlock.addInstructions(instructionBuilder.generateReturnInstr());
                        int num = -1;
                        basicBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
                        Label label = new Label(String.valueOf(num),new LabelType(num));
                        basicBlock.addOneInstruction(label);
                        basicBlocks.add(basicBlock);
                        return basicBlocks;
                    } else {
                        System.out.println("error");
                    }
                    pos++;
                }
            }
        }
        return basicBlocks;
    }

    public void dealParams(BasicBlock basicBlock,ArrayList<Symbol> params,SymbolTable symbolTable) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (Symbol symbol : params) {
            String name = "%" + funcCnt.getCnt();
            LlvmIrValue llvmIrValue = symbolTable.getVarByName(symbol.getName()).getLlvmIrValue();
            Alloca alloca = new Alloca(llvmIrValue.getType(),name,llvmIrValue);
            instructions.add(alloca);
            Store store = new Store("store",null,llvmIrValue,alloca);
            symbolTable.getVarByName(symbol.getName()).setLlvmIrValue(alloca);
            symbol.setLlvmIrValue(alloca);
            instructions.add(store);
        }
        basicBlock.addInstructions(instructions);
    }

    public void generateDecl(BasicBlock basicBlock, DeclNode declNode,SymbolTable symbolTable) {
        InstructionBuilder instructionBuilder = new InstructionBuilder(declNode,basicBlock,symbolTable,funcCnt);
        ArrayList<Instruction> instructions = instructionBuilder.generateInstruction();
        basicBlock.addInstructions(instructions);
    }

    public void generateStmt(BasicBlock basicBlock, StmtNode stmtNode,SymbolTable symbolTable) {
        InstructionBuilder instructionBuilder = new InstructionBuilder(stmtNode,basicBlock,symbolTable,funcCnt);
        ArrayList<Instruction> instructions = instructionBuilder.generateInstruction();
        basicBlock.addInstructions(instructions);
    }

    public ArrayList<BasicBlock> dealIf(BasicBlock basicBlock,SymbolTable symbolTable,StmtNode stmtNode) {
        ArrayList<BasicBlock> bbs = new ArrayList<>();
        CondNode condNode = stmtNode.getCondNode();
        StmtNode stmtNode_if = stmtNode.getStmtNode();
        StmtNode stmtNode_else = stmtNode.getStmtNode_else();
        ArrayList<Br> brNeedTrueValue = new ArrayList<>(); //需要为真跳转的
        ArrayList<Br> brNeedFalseValue = new ArrayList<>();

        dealCondNode(basicBlock,condNode,symbolTable,bbs,brNeedTrueValue,brNeedFalseValue);
        //此时，bbs里面是所有条件判断产生的块，两个br都是未填值的br,true 的都是进if 的，false都是进if后的
        int num = funcCnt.getCnt();
        BasicBlock bb = new BasicBlock(String.valueOf(num),new LabelType(num));
        Label label = new Label(String.valueOf(num),new LabelType(num));
        bb.addOneInstruction(label);
        bbs.add(bb); //这个是if 的语句块

        for (Br br : brNeedTrueValue) { //填跳转到if
            if (br.getHasTrueLabel() && br.getTrueLabel() == null) {
                br.setTrueLabel(bb);
            }
        }
        brNeedTrueValue = new ArrayList<>();

        SymbolTable newSymboltable = new SymbolTable(symbolTable); //if 处理
        symbolTable.addChild(newSymboltable);
        ArrayList<BasicBlock> ifBlocks;
        if (stmtNode_if.getBlockNode() != null) {
            ifBlocks = generateBasicBlocks(bb, stmtNode_if.getBlockNode().getBlockItemNodes(), newSymboltable);
        } else {
            ArrayList<BlockItemNode> temp = new ArrayList<>();
            BlockItemNode bi = new BlockItemNode(stmtNode_if);
            temp.add(bi);
            ifBlocks = generateBasicBlocks(bb, temp, newSymboltable);
        }
        for (BasicBlock ifb : ifBlocks) {
            bbs.add(ifb);
        }
        //num = funcCnt.getCnt();
        Br ifBr = new Br(String.valueOf("br"),null); //if 语句块最后的跳转
        bbs.get(bbs.size() - 1).addOneInstruction(ifBr);
        Br elseBr = null;
        if (stmtNode_else != null) {
            num = funcCnt.getCnt();
            bb = new BasicBlock(String.valueOf(num),new LabelType(num));
            label = new Label(String.valueOf(num),new LabelType(num));
            bb.addOneInstruction(label);
            bbs.add(bb);

            fillFalseBr(brNeedFalseValue,bb);

            newSymboltable = new SymbolTable(symbolTable);
            symbolTable.addChild(newSymboltable);
            ArrayList<BasicBlock> elseBlocks;
            if (stmtNode_else.getBlockNode() != null) {
                elseBlocks = generateBasicBlocks(bb, stmtNode_else.getBlockNode().getBlockItemNodes(), newSymboltable);
            } else {
                //elseBlocks = dealIf(bb, newSymboltable,stmtNode_else);
                ArrayList<BlockItemNode> temp = new ArrayList<>();
                BlockItemNode bi = new BlockItemNode(stmtNode_else);
                temp.add(bi);
                elseBlocks = generateBasicBlocks(bb, temp, newSymboltable);
            }

            for (BasicBlock elseb : elseBlocks) {
                bbs.add(elseb);
            }
            //num = funcCnt.getCnt();
            elseBr = new Br(String.valueOf("br"),new LabelType(num));
            bbs.get(bbs.size()-1).addOneInstruction(elseBr);
        }
        num = funcCnt.getCnt(); //新建一个块
        bb = new BasicBlock(String.valueOf(num),new LabelType(num));
        label = new Label(String.valueOf(num),new LabelType(num));
        bb.addOneInstruction(label);
        bbs.add(bb);
        ifBr.setJump(bb);
        if (stmtNode_else == null) {
            fillFalseBr(brNeedFalseValue,bb); //if语句的结束跳转
        } else {
            elseBr.setJump(bb);
        }
        return bbs;
    }

    public void dealCondNode(BasicBlock bb,CondNode condNode,
                                               SymbolTable symbolTable,ArrayList<BasicBlock> basicBlocks,
                                               ArrayList<Br> brNeedTrueValue, ArrayList<Br> brNeedFalseValue) { // a || b
        LOrExpNode lOrExpNode = condNode.getlOrExpNode();
        ArrayList<Token> op = lOrExpNode.getOp();
        ArrayList<LAndExpNode> lAndExpNodes = lOrExpNode.getlAndExpNodes();
        ArrayList<Br> tbr = new ArrayList<>();
        ArrayList<Br> fbr = new ArrayList<>();
        ArrayList<BasicBlock> bbs = dealAndExpNode(bb,lAndExpNodes.get(0),symbolTable,tbr,fbr); //得到 || 一侧的所有基本块
        for (BasicBlock b : bbs) {
            basicBlocks.add(b);
        }
        for (int i = 0;i < op.size();i++) {
            //需要新建一个块，作为引物
            int num = funcCnt.getCnt();
            BasicBlock bsb = new BasicBlock(String.valueOf(num), new LabelType(num));
            bbs.add(bsb);
            Label label = new Label(String.valueOf(num), new LabelType(num));
            bsb.addOneInstruction(label);
            basicBlocks.add(bsb);
            //可以先填br的值,对于所有块，如果为假，则跳转至此；为真的话仍不知道，继续回传
            fillFalseBr(fbr,bsb);
            //剩下的加入回传的br组合
            addAllBrs(tbr,fbr,brNeedTrueValue,brNeedFalseValue);
            //继续计算
            tbr = new ArrayList<>();
            fbr = new ArrayList<>();
            bbs = dealAndExpNode(bsb,lAndExpNodes.get(i + 1),symbolTable,tbr,fbr);
            for (BasicBlock b : bbs) {
                basicBlocks.add(b);
            }
        }
        addAllBrs(tbr,fbr,brNeedTrueValue,brNeedFalseValue);
    }

    public void addAllBrs( ArrayList<Br> tbr,ArrayList<Br> fbr,
                           ArrayList<Br> brNeedTrueValue, ArrayList<Br> brNeedFalseValue) {
        for (Br br : tbr) {
            brNeedTrueValue.add(br);
        }
        for (Br br : fbr) {
            brNeedFalseValue.add(br);
        }
    }

    public void fillFalseBr(ArrayList<Br> brNeedFalseValue,BasicBlock tb) {
        Iterator<Br> iterator1 = brNeedFalseValue.iterator();
        while(iterator1.hasNext()) {
            Br br = iterator1.next();
                if (br.getHasFalseLabel() && br.getFalseLabel() == null) {
                br.setFalseLabel(tb);
                iterator1.remove();
            }
        }
    }

    public ArrayList<BasicBlock> dealAndExpNode(BasicBlock bb,LAndExpNode lAndExpNode,
                                                SymbolTable symbolTable,ArrayList<Br> brNeedTrueValue,
                                                ArrayList<Br> brNeedFalseValue) {
        ArrayList<BasicBlock> bbs = new ArrayList<>();
        ArrayList<EqExpNode> expNodes = lAndExpNode.getEqExpNodes();
        ArrayList<Token> op = lAndExpNode.getOp();
        //计算值，并且存入语句块
        LlvmIrValue cond = dealEqExp(bb,expNodes.get(0),symbolTable);
        String cmp_name = "%" + funcCnt.getCnt();
        String key = "ne";
        Calculate calculate = new Calculate(cmp_name, cond, new LlvmIrValue("0",new IntType(32)), InstructionType.icmp, new IntType(1), key);
        bb.addOneInstruction(calculate);
        //生成跳转，存入语句块
        //String name_br = "%" + funcCnt.getCnt();
        Br br = new Br("br", null, calculate);
        bb.addOneInstruction(br);
        //对于 && 两侧的每一个式子进行类似操作
        for (int i = 0; i < op.size(); i++) {
            //新建语句块
            int num = funcCnt.getCnt();
            BasicBlock bsb = new BasicBlock(String.valueOf(num), new LabelType(num));
            bbs.add(bsb);
            Label label = new Label(String.valueOf(num), new LabelType(num));
            bsb.addOneInstruction(label);
            //设置上一个语句块的 trueLabel
            br.setTrueLabel(bsb);  //上一个block的Br
            brNeedFalseValue.add(br);

            cond = dealEqExp(bsb, expNodes.get(i + 1), symbolTable);
            cmp_name = "%" + funcCnt.getCnt();
            calculate = new Calculate(cmp_name, cond, new LlvmIrValue("0", new IntType(32)), InstructionType.icmp, new IntType(1), key);
            bsb.addOneInstruction(calculate);
            //name_br = "%" + funcCnt.getCnt();
            br = new Br("br", null, calculate);
            bsb.addOneInstruction(br);
        }
        //最后一个语句块的 br 不论真假都不知道跳到哪里
        brNeedTrueValue.add(br);
        brNeedFalseValue.add(br);
        return bbs;
    }

    public LlvmIrValue dealEqExp(BasicBlock bb,EqExpNode eqExpNode,SymbolTable symbolTable) {
        ArrayList<RelExpNode> relExpNodes = eqExpNode.getRelExpNodes();
        ArrayList<Token> op =  eqExpNode.getOp();
        LlvmIrValue left = dealRelExp(bb,relExpNodes.get(0),symbolTable);
        LlvmIrValue right = null;
        if (op.size() > 0) {
            Zext zext = null;
            for (int i = 0; i < op.size(); i++) {
                right = dealRelExp(bb, relExpNodes.get(i + 1), symbolTable);
                String key;
                if (op.get(i).getTokenType() == TokenType.EQL) {
                    key = "eq";
                } else {
                    key = "ne";
                }
                String name = "%" + funcCnt.getCnt();
                Calculate calculate = new Calculate(name, left, right, InstructionType.icmp, new IntType(1), key);
                bb.addOneInstruction(calculate);
                name = "%" + funcCnt.getCnt();
                zext = new Zext(name,null,calculate);
                bb.addOneInstruction(zext);
                left = zext;
            }
            return zext;
        } else {
            return left;
        }
    }

    public LlvmIrValue dealRelExp(BasicBlock bb,RelExpNode relExpNode,SymbolTable symbolTable) {
        ArrayList<AddExpNode> addExpNodes = relExpNode.getAddExpNodes();
        ArrayList<Token> op = relExpNode.getOp();
        InstructionBuilder builder = new InstructionBuilder(symbolTable,funcCnt,addExpNodes.get(0));
        LlvmIrValue left = builder.generateSpecialInstructions(bb);
        LlvmIrValue right = null;
        if (op.size() > 0) {
            Zext zext = null;
            for (int i = 0; i < op.size(); i++) {
                InstructionBuilder build = new InstructionBuilder(symbolTable, funcCnt, addExpNodes.get(i + 1));
                right = build.generateSpecialInstructions(bb);
                String bond = "";
                if (op.get(i).getTokenType() == TokenType.LSS) {
                    bond = "slt";
                } else if (op.get(i).getTokenType() == TokenType.LEQ) {
                    bond = "sle";
                } else if (op.get(i).getTokenType() == TokenType.GRE) {
                    bond = "sgt";
                } else if (op.get(i).getTokenType() == TokenType.GEQ) {
                    bond = "sge";
                }
                String name = "%" + funcCnt.getCnt();
                Calculate calculate = new Calculate(name, left, right, InstructionType.icmp, new IntType(1), bond);
                bb.addOneInstruction(calculate);
                name = "%" + funcCnt.getCnt();
                zext = new Zext(name,null,calculate);
                bb.addOneInstruction(zext);
                left = zext;
            }
            return zext;
        } else {
            /*String name = "%" + funcCnt.getCnt();
            String bond = "sgt"; //只有一个数字，跟 0 比
            Calculate calculate = new Calculate(name,left,new LlvmIrValue("0",new IntType(32)),
                    InstructionType.icmp, new IntType(1), bond);
            bb.addOneInstruction(calculate);
            name = "%" + funcCnt.getCnt();
            Zext zext = new Zext(name,null,calculate);
            bb.addOneInstruction(zext);*/
            return left;
        }
    }

    public ArrayList<BasicBlock> dealFor(BasicBlock basicBlock,SymbolTable symbolTable,StmtNode stmtNode) {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
        ForStmtNode forStmtNode1 = stmtNode.getForStmtNode_1();
        CondNode condNode = stmtNode.getCondNode();
        ForStmtNode forStmtNode2 = stmtNode.getForStmtNode2();
        StmtNode forStmt = stmtNode.getStmtNode();
        if (forStmtNode1 != null) {
            InstructionBuilder instructionBuilder = new InstructionBuilder(symbolTable,funcCnt,forStmtNode1);
            instructionBuilder.generateForInstructions(basicBlock);
        }
        Br br = new Br("br",null);
        basicBlock.addOneInstruction(br);
        ArrayList<Br> brNeedTrueValue = new ArrayList<>(); //需要为真跳转的
        ArrayList<Br> brNeedFalseValue = new ArrayList<>();
        int num = funcCnt.getCnt(); //cond 块
        BasicBlock block = new BasicBlock(String.valueOf(num),new LabelType(num));
        Label label = new Label(String.valueOf(num),new LabelType(num));
        block.addOneInstruction(label);
        basicBlocks.add(block);

        br.setJump(block);

        SymbolTable newSymbolTable = new SymbolTable(symbolTable);
        symbolTable.addChild(newSymbolTable);
        BasicBlock for_body;
        if (condNode != null) {
            dealCondNode(block,condNode,newSymbolTable,basicBlocks,brNeedTrueValue,brNeedFalseValue);
            num = funcCnt.getCnt(); //for循环主体块的第一个块,从这里开始可能有break，continue
            for_body = new BasicBlock(String.valueOf(num),new LabelType(num));
            basicBlocks.add(for_body);
            label = new Label(String.valueOf(num),new LabelType(num));
            for_body.addOneInstruction(label);
        } else {
            //block 块可作为for循环的第一个块
            for_body = block;
        }
        newSymbolTable = new SymbolTable(symbolTable);
        symbolTable.addChild(newSymbolTable);
        ArrayList<BasicBlock> bbs;
        if (forStmt.getBlockNode() != null) {
            bbs = generateBasicBlocks(for_body, forStmt.getBlockNode().getBlockItemNodes(), newSymbolTable);
        } else {
            ArrayList<BlockItemNode> temp = new ArrayList<>();
            BlockItemNode bi = new BlockItemNode(forStmt);
            temp.add(bi);
            bbs = generateBasicBlocks(for_body, temp, newSymbolTable);
        }
        for (BasicBlock bs : bbs) {
            basicBlocks.add(bs); //for循环主体块的全部
        }
        br = new Br("br",null);
        basicBlocks.get(basicBlocks.size() - 1).addOneInstruction(br);
        num = funcCnt.getCnt();
        BasicBlock last_for = new BasicBlock(String.valueOf(num),new LabelType(num));
        label = new Label(String.valueOf(num),new LabelType(num));
        last_for.addOneInstruction(label);
        basicBlocks.add(last_for);
        br.setJump(last_for);
        if (forStmtNode2 != null) {
            InstructionBuilder instructionBuilder = new InstructionBuilder(symbolTable,funcCnt,forStmtNode2);
            instructionBuilder.generateForInstructions(last_for);
        }
        br = new Br("br",null);
        br.setJump(block);
        last_for.addOneInstruction(br);
        num = funcCnt.getCnt();
        BasicBlock nextBlock = new BasicBlock(String.valueOf(num),new LabelType(num));
        label = new Label(String.valueOf(num),new LabelType(num));
        nextBlock.addOneInstruction(label);
        basicBlocks.add(nextBlock);

        for (Br x : brNeedTrueValue) {
            if (x.getHasTrueLabel() && x.getTrueLabel() == null) {
                x.setTrueLabel(for_body);
            }
        }
        for (Br x : brNeedFalseValue) {
            if (x.getHasFalseLabel() && x.getFalseLabel() == null) {
                x.setFalseLabel(nextBlock);
            }
        }
        //检查 forBody 和 bbs的所有块，是否有break,continue break -> nextBlock,continue -> last_for
        for_body.setBC(nextBlock,last_for);
        for (BasicBlock b : bbs) {
            b.setBC(nextBlock,last_for);
        }
        return basicBlocks;
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
}

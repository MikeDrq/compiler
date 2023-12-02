package Mips.MipsBasicBlock;

import Middle.LlvmIrValue;
import Middle.Type.PointerType;
import Middle.Value.BasicBlock.BasicBlock;
import Middle.Value.Instruction.AllInstructions.*;
import Middle.Value.Instruction.Instruction;
import Middle.Value.Instruction.InstructionType;
import Mips.MipsFunc.StringCnt;
import Mips.MipsInstruction.*;
import Mips.MipsSymbolTable.MipsSymbol;
import Mips.MipsSymbolTable.MipsSymbolTable;
import Mips.Register;
import Mips.GlobalLabelCnt;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsBasicBlockBuilder {
    private BasicBlock basicBlock;
    private StringCnt stringCnt;
    private MipsSymbolTable mipsSymbolTable;
    private Register register;
    private int offset;
    private int fp_offset;
    private MipsBasicBlock mipsBasicBlock;
    private Boolean isMain;
    private HashMap<String,Integer> labelMatch;
    private GlobalLabelCnt gbc;

    public MipsBasicBlockBuilder(BasicBlock basicBlock, StringCnt stringCnt, MipsSymbolTable mipsSymbolTable,
                                 Register register, Boolean isMain, int offset, int fp_offset, HashMap<String,Integer
            > labelMatch,GlobalLabelCnt gbc) {
        this.basicBlock = basicBlock;
        this.stringCnt = stringCnt;
        this.mipsSymbolTable = mipsSymbolTable;
        this.register = register;
        this.offset = offset;
        this.fp_offset = fp_offset;
        this.mipsBasicBlock = new MipsBasicBlock();
        this.isMain = isMain;
        this.labelMatch = labelMatch;
        this.gbc = gbc;
    }

    public void setMipsSymbolTable(String name, MipsSymbol mipsSymbol) {
        mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
    }

    public int getOffset() {
        return this.offset;
    }

    public int getFpOffset() {
        return this.fp_offset;
    }

    public MipsBasicBlock generateMipsBasicBlock(ArrayList<String> strings) {
        ArrayList<Instruction> instructions = basicBlock.getInstructions();
        for (int i = 0;i < instructions.size();i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof Alloca) { //填个符号表
                dealAlloca(instruction);
            } else if (instruction instanceof Br) {
                //mips_2_br
                dealBr(instruction);
            } else if (instruction instanceof Calculate) {
                dealCalculate(instruction);
            } else if (instruction instanceof Getelementptr) {
                //mips_2_array
                dealGetelementptr(instruction);
            } else if (instruction instanceof Label) {
                //mips_2_label
                String name = instruction.getName();
                int num = acquireLabel(name);
                MipsLabel mipsLabel = new MipsLabel(num);
                mipsBasicBlock.addInstruction(mipsLabel);
            } else if (instruction instanceof Load) {
                dealLoad(instruction); //只需要t0,t1,t2,t3
            } else if (instruction instanceof Ret) {
                dealRet(instruction);
            } else if (instruction instanceof Store) {
                dealStore(instruction);
            } else if (instruction instanceof Zext) {
                //mips_2_zext
                dealZext(instruction);
            } else if (instruction instanceof Call) {
                if (((Call) instruction).getMark() == 3) {
                    StringBuilder sb = new StringBuilder();
                    while (i < instructions.size()) {
                        Instruction t = instructions.get(i);
                        if (!(t instanceof Call)) {
                            break;
                        }
                        if (((Call) t).getMark() != 3) {
                            break;
                        }
                        String c = ((Call) t).getChar();
                        if (c.equals("\n")) {
                            sb.append("\\n");
                        } else {
                            sb.append(c);
                        }
                        i++;
                    }
                    i--;
                    int num = stringCnt.getNum();
                    String name = "str" + num;
                    La la = new La(4,name); //la $a0,
                    mipsBasicBlock.addInstruction(la);
                    Li li = new Li(2,4); //li $v0,
                    mipsBasicBlock.addInstruction(li);
                    Syscall syscall = new Syscall(); //syscall
                    mipsBasicBlock.addInstruction(syscall);
                    strings.add(name + ": .asciiz \"" + sb.toString() + "\"\n");
                } else {
                    dealCall(instruction);
                }
            } else {
                System.out.println("instructions type error");
            }
        }
        return mipsBasicBlock;
    }

    public int findReg() {
        int reg = 0;
        for (int j = 8;j <=15;j++) {
            if (register.canUseNow(j)) {
                reg = j;
                register.useRegister(reg);
                break;
            }
        }
        if (reg == 0) {
            System.out.println("error when allocate registers");
        }
        return reg;
    }

    public int getOperandReg(String name) { //只能是右侧操作数，可以是%（一定在符号表），可以是@，可以是const
        int reg = 0;
        if (name.charAt(0) == '%') {
            reg = findReg();
            if (mipsSymbolTable.containsMipsSymbol(name)) {
                MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                int pos = mipsSymbol.getOffset();
                Lw lw = new Lw(reg,29,pos);
                mipsBasicBlock.addInstruction(lw);
            } else {
                System.out.println("should not happen,cannot find operand");
            }
        } else if (name.charAt(0) == '@') {
            reg = findReg();
            Lw lw = new Lw(reg,name.substring(1));
            mipsBasicBlock.addInstruction(lw);
        } else {
            reg = findReg();
            Li li = new Li(reg,Integer.parseInt(name));
            mipsBasicBlock.addInstruction(li);
        }
        return reg;
    }


    public int acquireLabel(String name) {
        if (labelMatch.containsKey(name)) {
            return labelMatch.get(name);
        } else {
            int num = gbc.getLabelCnt();
            labelMatch.put(name,num);
            return num;
        }
    }

    public void dealBr(Instruction instruction) {
        Br br = (Br) instruction;
        if (!br.getJumpWithNoCondition()) {
            String tLabel = br.getTrueLabel().getName();
            String fLabel = br.getFalseLabel().getName();
            int num = acquireLabel(fLabel);
            int reg = getOperandReg(br.getCondName());
            Beq beq = new Beq(reg,num);
            mipsBasicBlock.addInstruction(beq);
            num = acquireLabel(tLabel);
            J j = new J(num);
            mipsBasicBlock.addInstruction(j);
            register.freeRegister(reg);
        } else {
            String label = br.getJumpName();
            int num = acquireLabel(label);
            J j = new J(num);
            mipsBasicBlock.addInstruction(j);
        }
    }

    public void dealAlloca(Instruction instruction) {
        LlvmIrValue llvmIrValue = ((Alloca) instruction).getLlvmIrValue();
        if (llvmIrValue.getDim() == 0) {
            MipsSymbol mipsSymbol = new MipsSymbol(instruction.getName(), offset, llvmIrValue);
            setMipsSymbolTable(instruction.getName(), mipsSymbol);
            offset = offset - 4;
        } else if (llvmIrValue.getDim() == 1) {
            MipsSymbol mipsSymbol = new MipsSymbol(instruction.getName(),offset,llvmIrValue);
            setMipsSymbolTable(instruction.getName(),mipsSymbol);
            if (!(instruction.getType() instanceof PointerType)) {
                mipsSymbol.setFpOffset(fp_offset);
                int reg = findReg();
                Addi addi = new Addi(30, reg, fp_offset); //计算绝对首地址
                mipsBasicBlock.addInstruction(addi);
                Sw sw = new Sw(reg, 29, offset); //存入首地址
                mipsBasicBlock.addInstruction(sw);
                fp_offset = fp_offset + llvmIrValue.getColumn() * 4;
                offset = offset - 4;
                register.freeRegister(reg);
            } else {
                offset = offset - 4;
            }
        } else if (llvmIrValue.getDim() == 2) {
            MipsSymbol mipsSymbol = new MipsSymbol(instruction.getName(),offset,llvmIrValue);
            setMipsSymbolTable(instruction.getName(),mipsSymbol);
            if (!(instruction.getType() instanceof PointerType)) {
                mipsSymbol.setFpOffset(fp_offset);
                int reg = findReg();
                Addi addi = new Addi(30, reg, fp_offset); //计算绝对首地址
                mipsBasicBlock.addInstruction(addi);
                Sw sw = new Sw(reg, 29, offset); //存入首地址
                mipsBasicBlock.addInstruction(sw);
                int pos = llvmIrValue.getColumn() * llvmIrValue.getRaw() * 4;
                fp_offset = fp_offset + pos;
                offset = offset - 4;
                register.freeRegister(reg);
            } else {
                offset = offset - 4;
            }
        } else {
            System.out.println("Dim error!");
        }
    }

    public void dealCompare(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2) {
        String name = instruction.getName();
        String cond = ((Calculate) instruction).getCond();
        String name1 = operand1.getName();
        String name2 = operand2.getName();
        int reg1 = getOperandReg(name1); //ok
        int reg2 = getOperandReg(name2); //ok
        int reg3 = findReg();
        if (cond.equals("slt")) {
            Compare compare = new Compare("slt",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else if (cond.equals("sle")) {
            Compare compare = new Compare("sle",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else if (cond.equals("sgt")) {
            Compare compare = new Compare("sgt",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else if (cond.equals("sge")) {
            Compare compare = new Compare("sge",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else if (cond.equals("ne")) {
            Compare compare = new Compare("sgt",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
            int reg4 = findReg();
            compare = new Compare("slt",reg1,reg2,reg4);
            mipsBasicBlock.addInstruction(compare);
            MipsCalculate mipsCalculate = new MipsCalculate("or",reg3,reg4,reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
            register.freeRegister(reg4);
        } else if (cond.equals("eq")) {
            Compare compare = new Compare("sge",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
            int reg4 = findReg();
            compare = new Compare("sle",reg1,reg2,reg4);
            mipsBasicBlock.addInstruction(compare);
            MipsCalculate mipsCalculate = new MipsCalculate("and",reg3,reg4,reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
            register.freeRegister(reg4);
        } else {
            System.out.println("unexpected cond in icmp");
        }
        int pos = 0;
        if (name.charAt(0) == '%') {
            if (mipsSymbolTable.containsMipsSymbol(name)) {
                MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                pos = mipsSymbol.getOffset();
            } else {
                pos = offset;
                MipsSymbol mipsSymbol = new MipsSymbol(name,offset,instruction);
                offset = offset - 4;
                mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
            }
            Sw sw = new Sw(reg3,29,pos);
            mipsBasicBlock.addInstruction(sw);
        } else if (name.charAt(0) == '@') {
            Sw sw = new Sw(reg3,name.substring(1));
            mipsBasicBlock.addInstruction(sw);
        } else {
            System.out.println("wrong!");
        }
        register.freeRegister(reg1);
        register.freeRegister(reg2);
        register.freeRegister(reg3);
    }

    public void dealCalculate(Instruction instruction) {
        String name = instruction.getName(); // name = operand1 op operand2
        LlvmIrValue operand1 = ((Calculate) instruction).getLeft();
        LlvmIrValue operand2 = ((Calculate) instruction).getRight();
        String instr = "";
        if (((Calculate) instruction).getInstructionType() == InstructionType.add) {
            instr = "addu";
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.sub) {
            instr = "subu";
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.mul) {
            instr = "mul";
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.sdiv) {
            instr = "div";
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.srem) {
            instr = "srem";
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.icmp){
            dealCompare(instruction,operand1,operand2);
            return;
        } else {
            System.out.println("unexpected instr!");
        }
        String name1 = operand1.getName();
        String name2 = operand2.getName();
        int reg1 = getOperandReg(name1); //ok
        int reg2 = getOperandReg(name2); //ok
        int reg3 = findReg();
        MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
        mipsBasicBlock.addInstruction(mipsCalculate);
        if (instr.equals("srem")) {
            Mfhi mfhi = new Mfhi(reg1);
            mipsBasicBlock.addInstruction(mfhi);
            register.freeRegister(reg3);
            reg3 = reg1;
        }
        int pos = 0;
        if (name.charAt(0) == '%') {
            if (mipsSymbolTable.containsMipsSymbol(name)) {
                MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                pos = mipsSymbol.getOffset();
            } else {
                pos = offset;
                MipsSymbol mipsSymbol = new MipsSymbol(name,offset,instruction);
                offset = offset - 4;
                mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
            }
            Sw sw = new Sw(reg3,29,pos);
            mipsBasicBlock.addInstruction(sw);
        } else if (name.charAt(0) == '@') {
            Sw sw = new Sw(reg3,name.substring(1));
            mipsBasicBlock.addInstruction(sw);
        } else {
            System.out.println("wrong!");
        }
        register.freeRegister(reg1);
        register.freeRegister(reg2);
        register.freeRegister(reg3);
    }

    public void dealZext(Instruction instruction) {
        Zext zext = (Zext) instruction;
        String name = zext.getName();
        String valueName = zext.getValueName();
        int reg = getOperandReg(valueName);
        int pos = 0;
        if (name.charAt(0) == '%') {
            if (mipsSymbolTable.containsMipsSymbol(name)) {
                MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                pos = mipsSymbol.getOffset();
            } else {
                pos = offset;
                MipsSymbol mipsSymbol = new MipsSymbol(name,offset,instruction);
                offset = offset - 4;
                mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
            }
            Sw sw = new Sw(reg,29,pos);
            mipsBasicBlock.addInstruction(sw);
        } else if (name.charAt(0) == '@') {
            Sw sw = new Sw(reg,name.substring(1));
            mipsBasicBlock.addInstruction(sw);
        } else {
            System.out.println("wrong!");
        }
        register.freeRegister(reg);
    }

    public void dealStore(Instruction instruction) {
        LlvmIrValue left = ((Store) instruction).getLeftValue();
        LlvmIrValue right = ((Store) instruction).getRightValue();
        String nameLeft = left.getName();
        String nameRight = right.getName();
        if (right.getDim() == 0) {
            int reg1 = getOperandReg(nameLeft); //ok
            if (nameRight.charAt(0) == '%') {
                int pos = -1;
                if (mipsSymbolTable.containsMipsSymbol(nameRight)) {
                    MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(nameRight);
                    pos = mipsSymbol.getOffset();
                } else {
                    System.out.println("should not happen,cannot find operand");
                }
                Sw sw = new Sw(reg1, 29, pos);
                mipsBasicBlock.addInstruction(sw);
            } else if (nameRight.charAt(0) == '@') {
                Sw sw = new Sw(reg1, nameRight.substring(1));
                mipsBasicBlock.addInstruction(sw);
            } else {
                System.out.println("illegal name");
            }
            register.freeRegister(reg1);
        } else {
            if (!(left.getType() instanceof PointerType)) {
                int reg1 = getOperandReg(nameLeft);
                int reg2 = findReg();
                if (nameRight.charAt(0) == '%') {
                    int pos = -1;
                    if (mipsSymbolTable.containsMipsSymbol(nameRight)) {
                        MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(nameRight);
                        pos = mipsSymbol.getOffset();
                        Lw lw = new Lw(reg2, 29, pos);
                        mipsBasicBlock.addInstruction(lw);
                    } else {
                        System.out.println("should not happen,cannot find operand");
                    }
                    Sw sw = new Sw(reg1, reg2, 0);
                    mipsBasicBlock.addInstruction(sw);
                    register.freeRegister(reg1);
                    register.freeRegister(reg2);
                } else if (nameRight.charAt(0) == '@') {
                    System.out.println("weired");
                }
            } else {
                if (mipsSymbolTable.containsMipsSymbol(nameRight)) {
                    MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(nameRight);
                    int pos = mipsSymbol.getOffset();
                    int reg1 = getOperandReg(nameLeft);
                    Sw sw = new Sw(reg1,29,pos);
                    mipsBasicBlock.addInstruction(sw);
                } else {
                    System.out.println("should not happen,cannot find operand");
                }
            }
        }
    }

    public void dealGetelementptr(Instruction instruction) {
        Getelementptr getelementptr = (Getelementptr) instruction;
        String name = getelementptr.getName();
        MipsSymbol mipsSymbol; //相对偏移的位置存入栈的位置
        if (mipsSymbolTable.containsMipsSymbol(name)) {
            mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
        } else {
            int pos = offset;
            offset = offset - 4;
            mipsSymbol = new MipsSymbol(name,pos,instruction);
            mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
        }
        if (getelementptr.getDim() == 1) {
            String column = getelementptr.getOffsetColumn();
            int reg = getOperandReg(column);
            Sll sll = new Sll(reg,reg,2);
            mipsBasicBlock.addInstruction(sll);
            String base = getelementptr.getBaseName();
            int reg1 = findReg();
            if (base.charAt(0) == '%') {
                if (mipsSymbolTable.containsMipsSymbol(base)) {
                    MipsSymbol mipsSymbol1 = mipsSymbolTable.getMipsSymbol(base);
                    int pos = mipsSymbol1.getOffset(); //取出数组首地址的存放位置
                    Lw lw = new Lw(reg1,29,pos);
                    mipsBasicBlock.addInstruction(lw);
                    MipsCalculate mc = new MipsCalculate("addu",reg,reg1,reg);
                    mipsBasicBlock.addInstruction(mc);
                }
            } else {
                La la = new La(reg1,base.substring(1));
                mipsBasicBlock.addInstruction(la);
                MipsCalculate mipsCalculate1 = new MipsCalculate("addu",reg,reg1,reg);
                mipsBasicBlock.addInstruction(mipsCalculate1);
            }
            Sw sw = new Sw(reg,29, mipsSymbol.getOffset());
            mipsBasicBlock.addInstruction(sw);
            register.freeRegister(reg);
            register.freeRegister(reg1);
        } else if (getelementptr.getDim() == 2) {
            String row = getelementptr.getOffsetRow();
            int reg1 = getOperandReg(row);
            int reg2 = findReg();
            int reg3;
            Li li = new Li(reg2, getelementptr.getColumnNum());
            mipsBasicBlock.addInstruction(li);
            String column = getelementptr.getOffsetColumn();
            reg3 = getOperandReg(column);
            MipsCalculate mipsCalculate = new MipsCalculate("mul", reg2, reg1, reg2);
            mipsBasicBlock.addInstruction(mipsCalculate);
            mipsCalculate = new MipsCalculate("addu", reg2, reg3, reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
            Sll sll = new Sll(reg3, reg3, 2);
            mipsBasicBlock.addInstruction(sll);
            String base = getelementptr.getBaseName();
            int reg4 = findReg();
            if (base.charAt(0) == '%') {
                if (mipsSymbolTable.containsMipsSymbol(base)) {
                    MipsSymbol mipsSymbol1 = mipsSymbolTable.getMipsSymbol(base);
                    int pos = mipsSymbol1.getOffset();
                    Lw lw = new Lw(reg4,29,pos);
                    mipsBasicBlock.addInstruction(lw);
                    MipsCalculate mc = new MipsCalculate("addu",reg4,reg3,reg3);
                    mipsBasicBlock.addInstruction(mc);
                }
            } else {
                La la = new La(reg4,base.substring(1));
                mipsBasicBlock.addInstruction(la);
                MipsCalculate mipsCalculate1 = new MipsCalculate("addu",reg3,reg4,reg3);
                mipsBasicBlock.addInstruction(mipsCalculate1);
            }
            Sw sw = new Sw(reg3, 29, mipsSymbol.getOffset());
            mipsBasicBlock.addInstruction(sw);
            register.freeRegister(reg1);
            register.freeRegister(reg2);
            register.freeRegister(reg3);
            register.freeRegister(reg4);
            mipsSymbolTable.addMipsSymbol(getelementptr.getName(),mipsSymbol);
        }
     }

    public void dealLoad(Instruction instruction) {
        String name = instruction.getName();
        LlvmIrValue right = ((Load) instruction).getLlvmIrValue();
        if (right.getDim() == 0) {
            int reg = getOperandReg(right.getName()); //从哪里load，一定已经声明，ok
            if (name.charAt(0) == '%') {
                if (mipsSymbolTable.containsMipsSymbol(name)) {
                    int pos = mipsSymbolTable.getMipsSymbol(name).getOffset();
                    Sw sw = new Sw(reg, 29, pos);
                    mipsBasicBlock.addInstruction(sw);
                } else {
                    int pos = offset;
                    offset = offset - 4;
                    MipsSymbol mipsSymbol = new MipsSymbol(name, pos, instruction);
                    mipsSymbolTable.addMipsSymbol(name, mipsSymbol);
                    Sw sw = new Sw(reg, 29, pos);
                    mipsBasicBlock.addInstruction(sw);
                }
            } else if (name.charAt(0) == '@') {
                Sw sw = new Sw(reg, name.substring(1));
                mipsBasicBlock.addInstruction(sw);
            } else {
                System.out.println("load wrong");
            }
            register.freeRegister(reg);
        } else {
            String rightName = right.getName();
            if (rightName.charAt(0) != '%') {
                System.out.println("weired");
            }
            int reg = getOperandReg(rightName);

            //MipsSymbol rightSymbol = mipsSymbolTable.getMipsSymbol(rightName);
            if (!(instruction.getType() instanceof PointerType)) {
                Lw lw = new Lw(reg,reg,0);
                mipsBasicBlock.addInstruction(lw);
            }
            if (name.charAt(0) == '%') {
                if (mipsSymbolTable.containsMipsSymbol(name)) {
                    int pos = mipsSymbolTable.getMipsSymbol(name).getOffset();
                    Sw sw = new Sw(reg, 29, pos);
                    mipsBasicBlock.addInstruction(sw);
                } else {
                    int pos = offset;
                    offset = offset - 4;
                    MipsSymbol mipsSymbol = new MipsSymbol(name, pos, instruction);
                    mipsSymbolTable.addMipsSymbol(name, mipsSymbol);
                    Sw sw = new Sw(reg, 29, pos);
                    mipsBasicBlock.addInstruction(sw);
                }
            } else if (name.charAt(0) == '@') {
                Sw sw = new Sw(reg, name.substring(1));
                mipsBasicBlock.addInstruction(sw);
            } else {
                System.out.println("load wrong");
            }
            register.freeRegister(reg);
        }
    }

    public void dealRet(Instruction instruction) { //存入 V0
        Ret ret = (Ret) instruction;
        Boolean isVoid = ret.getIsVoid();
        if (isVoid) {
            Jr jr = new Jr(31);
            mipsBasicBlock.addInstruction(jr);
        } else {
            LlvmIrValue retValue = ret.getRight();
            String name = retValue.getName();
            int reg = getOperandReg(name); //返回值，一定已存入符号表，ok
            Move move = new Move(2,reg);
            mipsBasicBlock.addInstruction(move);
            if (isMain) {
                Li li = new Li(2,10);
                mipsBasicBlock.addInstruction(li);
                Syscall syscall = new Syscall();
                mipsBasicBlock.addInstruction(syscall);
            } else {
                Jr jr = new Jr(31);
                mipsBasicBlock.addInstruction(jr);
            }
            register.freeRegister(reg);
        }
     }

     public void dealCall(Instruction instruction) {
         Call call = (Call) instruction;
         if (call.getMark() == 2) {
             LlvmIrValue llvmIrValue = call.getOp();
             String name = llvmIrValue.getName();
             int reg = getOperandReg(name); //肯定在符号表中
             if (register.canUseNow(4)) {
                 Move move = new Move(4,reg);
                 mipsBasicBlock.addInstruction(move);
                 Li li = new Li(2,1);
                 mipsBasicBlock.addInstruction(li);
                 Syscall syscall = new Syscall();
                 mipsBasicBlock.addInstruction(syscall);
             } else {
                 System.out.println("a0 should not be occupied when call putInt");
             }
             register.freeRegister(reg);
         } else if (call.getMark() == 1) {
             Li li = new Li(2,5);
             mipsBasicBlock.addInstruction(li);
             Syscall syscall = new Syscall();
             mipsBasicBlock.addInstruction(syscall);//v0 已沦陷
             String name = call.getName();
             if (name.charAt(0) == '%') {
                 if (mipsSymbolTable.containsMipsSymbol(name)) {
                     MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                     int pos = mipsSymbol.getOffset();
                     Sw sw = new Sw(2,29,pos);
                     mipsBasicBlock.addInstruction(sw);
                 } else {
                     int pos = offset;
                     MipsSymbol mipsSymbol = new MipsSymbol(name,offset,call);
                     offset = offset - 4;
                     mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                     Sw sw = new Sw(2,29,pos);
                     mipsBasicBlock.addInstruction(sw);
                 }
             } else if (name.charAt(0) == '@') {
                 Sw sw = new Sw(2,name);
                 mipsBasicBlock.addInstruction(sw);
             } else {
                 System.out.println("some problems when call getInt");
             }
         } else if (call.getMark() == 0) {
             //保存现场（暂时没啥好存的）直接内存存储，进入函数后
             ArrayList<LlvmIrValue> params = call.getParams(); //params.size() == 0,无参数;
             for (int i = params.size() - 1;i >= 0;i--) {
                 LlvmIrValue param = params.get(i);
                 String name = param.getName();
                 int reg = getOperandReg(name);
                 Sw sw = new Sw(reg,29,offset); //内存的位置存进去
                 offset = offset - 4;
                 mipsBasicBlock.addInstruction(sw);
                 register.freeRegister(reg);
             }
             Sw sw = new Sw(31,29,offset); //存入ra
             offset = offset - 4;
             mipsBasicBlock.addInstruction(sw);
             Addi addi = new Addi(29,29,offset); //函数的sp从此开始
             mipsBasicBlock.addInstruction(addi);
             Addi addi1 = new Addi(30,30,fp_offset);
             mipsBasicBlock.addInstruction(addi1);

             Jal jal = new Jal(call.getFuncValue().getName().substring(1));

             mipsBasicBlock.addInstruction(jal);
             Addi addi2 = new Addi(29,29,-offset);
             mipsBasicBlock.addInstruction(addi2);
             Addi addi3 = new Addi(30,30,-fp_offset);
             mipsBasicBlock.addInstruction(addi3);
             offset = offset + 4; //ra出栈
             Lw lw = new Lw(31,29,offset);
             mipsBasicBlock.addInstruction(lw);
             offset = offset + params.size() * 4;//传参用的是栈，参数没用了
             if (call.getName().length() > 0) { //有返回值需要存储
                 String name = call.getName();
                 if (name.charAt(0) == '%') {
                     if (mipsSymbolTable.containsMipsSymbol(name)) {
                        MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                        int pos = mipsSymbol.getOffset();
                        Sw sw1 = new Sw(2,29,pos);
                        mipsBasicBlock.addInstruction(sw1);
                     } else {
                         int pos = offset;
                         offset = offset - 4;
                         MipsSymbol mipsSymbol = new MipsSymbol(name,pos,call);
                         mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                         Sw sw1 = new Sw(2,29,pos);
                         mipsBasicBlock.addInstruction(sw1);
                     }
                 } else if (name.charAt(0) == '@') {
                     Sw sw1 = new Sw(2,name);
                     mipsBasicBlock.addInstruction(sw1);
                 }
             }
             register.freeRegister(2);
         } else {
             System.out.println("check function call when generating mips");
         }
     }
}

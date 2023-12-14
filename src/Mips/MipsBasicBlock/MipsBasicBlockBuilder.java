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
import SymbolTable.SymbolTable;

import java.util.*;

public class MipsBasicBlockBuilder {
    private BasicBlock basicBlock;
    private StringCnt stringCnt;
    private MipsSymbolTable mipsSymbolTable;
    private Register register;
    private int offset;
    private int fp_offset;
    private MipsBasicBlock mipsBasicBlock;
    private Boolean isMain;
    private HashMap<LlvmIrValue,Integer> varReg;

    public MipsBasicBlockBuilder(BasicBlock basicBlock, StringCnt stringCnt, MipsSymbolTable mipsSymbolTable,
                                 Register register, Boolean isMain, int offset, int fp_offset, HashMap<LlvmIrValue,Integer> varReg) {
        this.basicBlock = basicBlock;
        this.stringCnt = stringCnt;
        this.mipsSymbolTable = mipsSymbolTable;
        this.register = register;
        this.offset = offset;
        this.fp_offset = fp_offset;
        this.mipsBasicBlock = new MipsBasicBlock();
        this.isMain = isMain;
        this.varReg = varReg;
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
        LinkedList<Instruction> instructions = basicBlock.getInstructions();
        for (int i = 0;i < instructions.size();i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof Alloca) { //只有栈中存储的，才填符号表
                dealAlloca(instruction);
            } else if (instruction instanceof Br) {
                dealBr(instruction);
            } else if (instruction instanceof Calculate) {
                dealCalculate(instruction);
            } else if (instruction instanceof Getelementptr) {
                dealGetelementptr(instruction);
            } else if (instruction instanceof Label) {
                String name = instruction.getName();
                MipsLabel mipsLabel = new MipsLabel(name);
                mipsBasicBlock.addInstruction(mipsLabel);
            } else if (instruction instanceof Load) {
                dealLoad(instruction);
            } else if (instruction instanceof Ret) {
                dealRet(instruction);
            } else if (instruction instanceof Store) {
                dealStore(instruction);
            } else if (instruction instanceof Zext) {
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
            } else if (instruction instanceof  MidMove){
                dealMidMove(instruction);
            } else {
                System.out.println("instructions type error");
            }
        }
        return mipsBasicBlock;
    }

    public Boolean isAllocated(LlvmIrValue llvmIrValue) {
        if (varReg.containsKey(llvmIrValue)) {
            return true;
        }
        return false;
    }

    public int getReg(LlvmIrValue llvmIrValue) {
        return varReg.get(llvmIrValue);
    }

    public void findFromMem(String name,int num) {
        MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
        if (name.charAt(0) == '%') {
            int pos = mipsSymbol.getOffset();
            Lw lw = new Lw(num,29,pos);
            mipsBasicBlock.addInstruction(lw);
        } else {
            Lw lw = new Lw(num,name.substring(1));
            mipsBasicBlock.addInstruction(lw);
        }
    }

    public Boolean isConstant(String name) {
        if (name.charAt(0) == '@' || name.charAt(0) == '%') {
            return false;
        }
        return true;
    }

    public void dealBr(Instruction instruction) {
        Br br = (Br) instruction;
        if (!br.getJumpWithNoCondition()) {
            String tLabel = br.getTrueLabel().getName();
            String fLabel = br.getFalseLabel().getName();
            int reg = 26;
            if (isAllocated(br.getCond())) {
                reg = getReg(br.getCond());
            } else {
                MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(br.getCond().getName());
                Lw lw = new Lw(reg,29,mipsSymbol.getOffset());
                mipsBasicBlock.addInstruction(lw);
            }
            Beq beq = new Beq(reg,fLabel);
            mipsBasicBlock.addInstruction(beq);
            J j = new J(tLabel);
            mipsBasicBlock.addInstruction(j);
        } else {
            String label = br.getJumpName();
            J j = new J(label);
            mipsBasicBlock.addInstruction(j);
        }
    }

    public void dealAlloca(Instruction instruction) {
        LlvmIrValue llvmIrValue = ((Alloca) instruction).getLlvmIrValue();
        if (llvmIrValue.getDim() == 0) {
            System.out.println("ERROR!Var do not use alloca!");
        } else if (llvmIrValue.getDim() == 1) { //只有第一次定义的数组才有alloca，函数传参也改了
            MipsSymbol mipsSymbol = new MipsSymbol(instruction.getName(),offset,llvmIrValue);
            setMipsSymbolTable(instruction.getName(),mipsSymbol);
            if (isAllocated(instruction)) {
                int reg = getReg(instruction);
                Addi addi = new Addi(30, reg, fp_offset);
                mipsBasicBlock.addInstruction(addi);
                fp_offset = fp_offset + llvmIrValue.getColumn() * 4;
            } else {
                int reg = 26;
                Addi addi = new Addi(30, reg, fp_offset); //计算绝对首地址
                mipsBasicBlock.addInstruction(addi);
                Sw sw = new Sw(reg, 29, offset); //存入首地址
                mipsBasicBlock.addInstruction(sw);
                fp_offset = fp_offset + llvmIrValue.getColumn() * 4;
                offset = offset - 4;
            }
        } else if (llvmIrValue.getDim() == 2) {
            MipsSymbol mipsSymbol = new MipsSymbol(instruction.getName(),offset,llvmIrValue);
            setMipsSymbolTable(instruction.getName(),mipsSymbol);
            if (varReg != null && isAllocated(llvmIrValue)) {
                int reg = getReg(instruction);
                Addi addi = new Addi(30, reg, fp_offset);
                mipsBasicBlock.addInstruction(addi);
                int pos = llvmIrValue.getColumn() * llvmIrValue.getRaw() * 4;
                fp_offset = fp_offset + pos;
            } else {
                int reg = 26;
                Addi addi = new Addi(30, reg, fp_offset); //计算绝对首地址
                mipsBasicBlock.addInstruction(addi);
                Sw sw = new Sw(reg, 29, offset); //存入首地址
                mipsBasicBlock.addInstruction(sw);
                int pos = llvmIrValue.getColumn() * llvmIrValue.getRaw() * 4;
                fp_offset = fp_offset + pos;
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
        int reg1 = 26;
        int reg2 = 27;
        int reg3 = 26;
        if (!isConstant(name1)) {
            System.out.println(name1);
            if (isAllocated(operand1)) {
                reg1 = getReg(operand1);
            } else {
                findFromMem(name1,reg1);
            }
        } else {
            Li li = new Li(reg1,Integer.parseInt(name1));
            mipsBasicBlock.addInstruction(li);
        }
        if (!isConstant(name2)) {
            if (isAllocated(operand2)) {
                reg2 = getReg(operand2);
            } else {
                findFromMem(name2,reg2);
            }
        } else {
            Li li = new Li(reg2,Integer.parseInt(name2));
            mipsBasicBlock.addInstruction(li);
        }
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
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
            Compare compare = new Compare("sne",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else if (cond.equals("eq")) {
            Compare compare = new Compare("seq",reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(compare);
        } else {
            System.out.println("unexpected cond in icmp");
        }
        if (!isAllocated(instruction)) {
            if (name.charAt(0) == '%') {
                int pos;
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
            }
        }
    }

    public int dealOperand(LlvmIrValue operand,int op) {
        int reg;
        if (op == 1) {
            reg = 26;
        } else {
            reg = 27;
        }
        if (!isConstant(operand.getName())) {
            if (isAllocated(operand)) {
                reg = getReg(operand);
            } else {
                findFromMem(operand.getName(),reg);
            }
        } else {
            Li li = new Li(reg,Integer.parseInt(operand.getName()));
            mipsBasicBlock.addInstruction(li);
        }
        return reg;
    }

    public void dealAns(Instruction instruction,String name,int reg3) {
        if (!isAllocated(instruction)) {
            MipsSymbol mipsSymbol = new MipsSymbol(name,offset,instruction);
            mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
            if (name.charAt(0) == '%') {
                Sw sw = new Sw(reg3,29,offset);
                mipsBasicBlock.addInstruction(sw);
                offset = offset - 4;
            } else {
                Sw sw = new Sw(reg3,name.substring(1));
                mipsBasicBlock.addInstruction(sw);
            }
        }
    }

    public void isAdd(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2,String instr) {
        int reg1;
        int reg2;
        int reg3 = 26;
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        if (isConstant(operand1.getName())) {
            reg2 = dealOperand(operand2,2);
            Addi addi = new Addi(reg2,reg3,Integer.parseInt(operand1.getName()));
            mipsBasicBlock.addInstruction(addi);
        } else if (isConstant(operand2.getName())) {
            reg1 = dealOperand(operand1,1);
            Addi addi = new Addi(reg1,reg3,Integer.parseInt(operand2.getName()));
            mipsBasicBlock.addInstruction(addi);
        } else {
            reg1 = dealOperand(operand1,1);
            reg2 = dealOperand(operand2,2);
            MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
        }
        dealAns(instruction,instruction.getName(),reg3);
    }

    public void isSub(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2,String instr) {
        int reg1;
        int reg2;
        int reg3 = 26;
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        if (isConstant(operand2.getName())) {
            reg1 = dealOperand(operand1,1);
            Addi addi = new Addi(reg1,reg3,-1 * Integer.parseInt(operand2.getName()));
            mipsBasicBlock.addInstruction(addi);
        } else {
            reg1 = dealOperand(operand1,1);
            reg2 = dealOperand(operand2,2);
            MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
        }
        dealAns(instruction,instruction.getName(),reg3);
    }

    public void isMult(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2,String instr) {
        int reg1;
        int reg2;
        int reg3 = 26;
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        if (isConstant(operand1.getName())) {
            reg2 = dealOperand(operand2,2);
            Mul mul = new Mul(reg2,reg3,Integer.parseInt(operand1.getName()));
            mipsBasicBlock.addInstruction(mul);
        } else if (isConstant(operand2.getName())) {
            reg1 = dealOperand(operand1,1);
            Mul mul = new Mul(reg1,reg3,Integer.parseInt(operand2.getName()));
            mipsBasicBlock.addInstruction(mul);
        } else {
            reg1 = dealOperand(operand1,1);
            reg2 = dealOperand(operand2,2);
            MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
            mipsBasicBlock.addInstruction(mipsCalculate);
        }
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        dealAns(instruction,instruction.getName(),reg3);
    }

    public void isDiv(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2,String instr) {
        int reg1 = dealOperand(operand1,1);
        int reg2 = dealOperand(operand2,2);
        int reg3 = 26;
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
        mipsBasicBlock.addInstruction(mipsCalculate);
        Mflo mflo = new Mflo(reg3);
        mipsBasicBlock.addInstruction(mflo);
        dealAns(instruction,instruction.getName(),reg3);
    }

    public void isSrem(Instruction instruction,LlvmIrValue operand1,LlvmIrValue operand2,String instr) {
        int reg1 = dealOperand(operand1,1);
        int reg2 = dealOperand(operand2,2);
        int reg3 = 26;
        if (isAllocated(instruction)) {
            reg3 = getReg(instruction);
        }
        MipsCalculate mipsCalculate = new MipsCalculate(instr,reg1,reg2,reg3);
        mipsBasicBlock.addInstruction(mipsCalculate);
        Mfhi mfhi = new Mfhi(reg3);
        mipsBasicBlock.addInstruction(mfhi);
        dealAns(instruction,instruction.getName(),reg3);
    }

    public void dealCalculate(Instruction instruction) {
        LlvmIrValue operand1 = ((Calculate) instruction).getLeft();
        LlvmIrValue operand2 = ((Calculate) instruction).getRight();
        String instr = "";
        if (((Calculate) instruction).getInstructionType() == InstructionType.add) {
            instr = "addu";
            isAdd(instruction,operand1,operand2,instr);
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.sub) {
            instr = "subu";
            isSub(instruction,operand1,operand2,instr);
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.mul) {
            instr = "mul";
            isMult(instruction,operand1,operand2,instr);
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.sdiv) {
            instr = "div";
            isDiv(instruction,operand1,operand2,instr);
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.srem) {
            instr = "srem";
            isSrem(instruction,operand1,operand2,instr);
        } else if (((Calculate) instruction).getInstructionType() == InstructionType.icmp){
            dealCompare(instruction,operand1,operand2);
        } else {
            System.out.println("unexpected instr!");
        }
    }

    public void dealZext(Instruction instruction) {
        Zext zext = (Zext) instruction;
        String name = zext.getName();
        LlvmIrValue value = zext.getValue();
        String valueName = zext.getValueName();
        int reg1 = 26;
        int reg2 = 26;
        if (isAllocated(value)) {
            reg1 = getReg(value);
        } else {
            if (isConstant(valueName)) {
                Li li = new Li(reg1,Integer.parseInt(valueName));
                mipsBasicBlock.addInstruction(li);
            } else {
                findFromMem(valueName,reg1);
            }
        }
        if (isAllocated(instruction)) {
            reg2 =getReg(instruction);
            Move move = new Move(reg2,reg1);
            mipsBasicBlock.addInstruction(move);
        } else {
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
                Sw sw = new Sw(reg1,29,pos);
                mipsBasicBlock.addInstruction(sw);
            } else if (name.charAt(0) == '@') {
                Sw sw = new Sw(reg1,name.substring(1));
                mipsBasicBlock.addInstruction(sw);
            }
        }
    }

    public void dealStore(Instruction instruction) {
        LlvmIrValue left = ((Store) instruction).getLeftValue();
        LlvmIrValue right = ((Store) instruction).getRightValue();
        String nameLeft = left.getName();
        String nameRight = right.getName();
        if (right.getDim() == 0) {
            int reg = 26;
            if (isConstant(nameLeft)) {
                Li li = new Li(reg,Integer.parseInt(nameLeft));
                mipsBasicBlock.addInstruction(li);
            } else {
                if (isAllocated(left)) {
                    reg = getReg(left);
                } else {
                    MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(nameLeft);
                    int pos = mipsSymbol.getOffset();
                    Lw lw = new Lw(reg, 29, pos);
                    mipsBasicBlock.addInstruction(lw);
                }
            }
            Sw sw = new Sw(reg, nameRight.substring(1));
            mipsBasicBlock.addInstruction(sw);
        } else {
            int reg1 = 26;
            int reg2 = 27;
            if (isConstant(left.getName())) {
                Li li = new Li(reg1,Integer.parseInt(left.getName()));
                mipsBasicBlock.addInstruction(li);
            } else {
                if (isAllocated(left)) {
                    reg1 = getReg(left);
                } else {
                    findFromMem(left.getName(), reg1);
                }
            }
            if (isAllocated(right)) {
                reg2 = getReg(right);
            } else {
                findFromMem(right.getName(),reg2);
            }
            Sw sw = new Sw(reg1,reg2,0);
            mipsBasicBlock.addInstruction(sw);
        }
    }

    public void dealGetelementptr(Instruction instruction) {
        Getelementptr getelementptr = (Getelementptr) instruction;
        String name = getelementptr.getName();
        MipsSymbol mipsSymbol; //相对偏移的位置存入栈的位置
        int regK1 = 27;
        int reg = 26; //结果
        if (getelementptr.getDim() == 1) {
            LlvmIrValue column = getelementptr.getOffsetColumn();
            if (isConstant(column.getName())) {
                Li li = new Li(reg,Integer.parseInt(column.getName())); //reg1存入数据
                mipsBasicBlock.addInstruction(li);
            } else {
                if (isAllocated(column)) {
                    reg = varReg.get(column);
                } else {
                    findFromMem(column.getName(), reg);
                }
            }
            //此时reg1的值为column，
            Sll sll = new Sll(regK1,reg,2); //把结果存入27
            mipsBasicBlock.addInstruction(sll);
            LlvmIrValue base = getelementptr.getBase();
            reg = 26; //reg复位
            if (isAllocated(base)) { //base在26或reg
                reg = getReg(base);
            } else {
                if (base.getName().charAt(0) == '%') {
                    findFromMem(base.getName(), reg);
                } else {
                    La la = new La(reg,base.getName().substring(1));
                    mipsBasicBlock.addInstruction(la);
                }
            }
            if (isAllocated(instruction)) {
                int ans = getReg(instruction);
                MipsCalculate mc = new MipsCalculate("addu",reg,regK1,ans);
                mipsBasicBlock.addInstruction(mc);
            } else {
                MipsCalculate mc = new MipsCalculate("addu",reg,regK1,regK1);
                mipsBasicBlock.addInstruction(mc);
                if (name.charAt(0) == '%') {
                    if (mipsSymbolTable.containsMipsSymbol(name)) {
                        mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                    } else {
                        int pos = offset;
                        offset = offset - 4;
                        mipsSymbol = new MipsSymbol(name,pos,instruction);
                        mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                    }
                    int pos = mipsSymbol.getOffset();
                    Sw sw = new Sw(regK1,29,pos);
                    mipsBasicBlock.addInstruction(sw);
                } else {
                    System.out.println("gete error!");
                }
            }
        } else if (getelementptr.getDim() == 2) {
            LlvmIrValue row = getelementptr.getOffsetRow();
            Li li = new Li(regK1, getelementptr.getColumnNum()); //数字
            mipsBasicBlock.addInstruction(li);
            if (isConstant(row.getName())) {
                li = new Li(reg,Integer.parseInt(row.getName()));
                mipsBasicBlock.addInstruction(li);
            } else {
                if (isAllocated(row)) {
                    reg = getReg(row);
                } else {
                    findFromMem(row.getName(), reg);
                }
            }
            MipsCalculate mipsCalculate = new MipsCalculate("mul", reg, regK1, regK1); //regK1是结果
            mipsBasicBlock.addInstruction(mipsCalculate);
            LlvmIrValue column = getelementptr.getOffsetColumn();
            reg = 26; //reg复位至26
            if (isConstant(column.getName())) {
                li = new Li(reg,Integer.parseInt(column.getName()));
                mipsBasicBlock.addInstruction(li);
            } else {
                if (isAllocated(column)) {
                    reg = getReg(column);
                } else {
                    findFromMem(column.getName(), reg);
                }
            }
            mipsCalculate = new MipsCalculate("addu", reg, regK1, regK1);
            mipsBasicBlock.addInstruction(mipsCalculate);
            Sll sll = new Sll(regK1, regK1, 2);
            mipsBasicBlock.addInstruction(sll);
            LlvmIrValue base = getelementptr.getBase();
            reg = 26; //reg复位，结果存储于27
            if (isAllocated(base)) {
                reg = getReg(base);
            } else {
                if (base.getName().charAt(0) == '%') {
                    findFromMem(base.getName(), reg);
                } else {
                    La la = new La(reg,base.getName().substring(1));
                    mipsBasicBlock.addInstruction(la);
                }
            }
            if (isAllocated(instruction)) {
                int ans = getReg(instruction);
                mipsCalculate = new MipsCalculate("addu", reg, regK1, ans);
                mipsBasicBlock.addInstruction(mipsCalculate);
            } else {
                mipsCalculate = new MipsCalculate("addu", reg, regK1, regK1);
                mipsBasicBlock.addInstruction(mipsCalculate);
                if (mipsSymbolTable.containsMipsSymbol(name)) {
                    mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                } else {
                    int pos = offset;
                    offset = offset - 4;
                    mipsSymbol = new MipsSymbol(name,pos,instruction);
                    mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                }
                int pos = mipsSymbol.getOffset();
                Sw sw = new Sw(regK1,29,pos);
                mipsBasicBlock.addInstruction(sw);
            }
        }
     }

    public void dealLoad(Instruction instruction) {
        String name = instruction.getName();
        LlvmIrValue right = ((Load) instruction).getLlvmIrValue();
        if (right.getDim() == 0) {
            int reg = 26;
            if (isAllocated(instruction)) {
                reg = getReg(instruction);
            }
           if (right.getName().charAt(0) == '@') {
                Lw lw = new Lw(reg,right.getName().substring(1));
                mipsBasicBlock.addInstruction(lw);
            } else {
                System.out.println("load wrong");
            }
            if (!isAllocated(instruction)) {
                MipsSymbol mipsSymbol = new MipsSymbol(name,offset,instruction);
                mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                Sw sw = new Sw(reg,29,offset);
                offset = offset - 4;
                mipsBasicBlock.addInstruction(sw);
            }
        } else {
            String rightName = right.getName();
            int reg = 26;
            if (isAllocated(right)) {
                reg = getReg(right);
            } else {
                findFromMem(rightName,reg);
            }
            //MipsSymbol rightSymbol = mipsSymbolTable.getMipsSymbol(rightName);
            /*if (!(instruction.getType() instanceof PointerType)) { //???
                Lw lw = new Lw(reg,reg,0);
                mipsBasicBlock.addInstruction(lw);
            }*/
            if (isAllocated(instruction)) {
                int reg1 = getReg(instruction);
                Lw lw = new Lw(reg1,reg,0);
                mipsBasicBlock.addInstruction(lw);
            } else {
                int reg1 = 27;
                Lw lw = new Lw(reg1,reg,0);
                mipsBasicBlock.addInstruction(lw);
                if (name.charAt(0) == '%') {
                    if (mipsSymbolTable.containsMipsSymbol(name)) {
                        int pos = mipsSymbolTable.getMipsSymbol(name).getOffset();
                        Sw sw = new Sw(reg1, 29, pos);
                        mipsBasicBlock.addInstruction(sw);
                    } else {
                        int pos = offset;
                        offset = offset - 4;
                        MipsSymbol mipsSymbol = new MipsSymbol(name, pos, instruction);
                        mipsSymbolTable.addMipsSymbol(name, mipsSymbol);
                        Sw sw = new Sw(reg1, 29, pos);
                        mipsBasicBlock.addInstruction(sw);
                    }
                } else if (name.charAt(0) == '@') {
                    Sw sw = new Sw(reg1, name.substring(1));
                    mipsBasicBlock.addInstruction(sw);
                }
            }
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
            int reg = 26;
            if (!isConstant(retValue.getName())) {
                if (isAllocated(retValue)) {
                    reg = getReg(retValue);
                } else {
                    findFromMem(name, 26);
                }
            } else {
                Li li = new Li(reg,Integer.parseInt(name));
                mipsBasicBlock.addInstruction(li);
            }
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
        }
     }

     public void dealCall(Instruction instruction) {
         Call call = (Call) instruction;
         if (call.getMark() == 2) {
             LlvmIrValue llvmIrValue = call.getOp();
             String name = llvmIrValue.getName();
             int reg = 26;
             if (isConstant(name)) {
                Li li = new Li(26,Integer.parseInt(name));
                mipsBasicBlock.addInstruction(li);
             } else {
                 if (isAllocated(llvmIrValue)) {
                     reg = getReg(llvmIrValue);
                 } else {
                     findFromMem(name, reg);
                 }
             }
             Move move = new Move(4,reg);
             mipsBasicBlock.addInstruction(move);
             Li li = new Li(2,1);
             mipsBasicBlock.addInstruction(li);
             Syscall syscall = new Syscall();
             mipsBasicBlock.addInstruction(syscall);
         } else if (call.getMark() == 1) {
             Li li = new Li(2,5);
             mipsBasicBlock.addInstruction(li);
             Syscall syscall = new Syscall();
             mipsBasicBlock.addInstruction(syscall);//v0 已沦陷
             String name = call.getName();
             if (!isConstant(name)) {
                 if (isAllocated(call)) {
                     Move move = new Move(getReg(call),2);
                     mipsBasicBlock.addInstruction(move);
                 } else {
                     if (name.charAt(0) == '%') {
                         int pos;
                         if (mipsSymbolTable.containsMipsSymbol(name)) {
                             MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(name);
                             pos = mipsSymbol.getOffset();
                         } else {
                             pos = offset;
                             offset = offset -4;
                             MipsSymbol mipsSymbol = new MipsSymbol(name,pos,call);
                             mipsSymbolTable.addMipsSymbol(name,mipsSymbol);
                         }
                         Sw sw = new Sw(2, 29, pos);
                         mipsBasicBlock.addInstruction(sw);
                     } else {
                         Sw sw = new Sw(2,name);
                         mipsBasicBlock.addInstruction(sw);
                     }
                 }
             } else {
                 System.out.println("some problems when call getInt");
             }
         } else if (call.getMark() == 0) {
             //保存现场（暂时没啥好存的）直接内存存储，进入函数后
             HashSet<Integer> temp = new HashSet<>(varReg.values());
             HashMap<Integer,Integer> reflection = new HashMap<>();
             ArrayList<Integer> uses = new ArrayList<>(temp);
             for (int i = 0;i < uses.size();i++) { //保存寄存器
                 Sw sw = new Sw(uses.get(i),29,offset);
                 mipsBasicBlock.addInstruction(sw);
                 reflection.put(uses.get(i), offset);
                 offset = offset - 4;
             }
             ArrayList<LlvmIrValue> params = call.getParams();
             for (int i = params.size() - 1;i >= 0;i--) { //参数放入a0或内存
                 int reg = 26;
                 int a1 = 5;
                 if (!isConstant(params.get(i).getName())) {
                     if (isAllocated(params.get(i))) {
                         reg = getReg(params.get(i));
                     } else {
                         findFromMem(params.get(i).getName(), reg);
                     }
                 } else {
                     Li li = new Li(reg,Integer.parseInt(params.get(i).getName()));
                     mipsBasicBlock.addInstruction(li);
                 }
                 if (i <= 2) {
                     if (reflection.containsKey(reg)) {
                         Lw lw = new Lw(a1 + i, 29, reflection.get(reg));
                         mipsBasicBlock.addInstruction(lw);
                     } else {
                         Move move = new Move(a1+i,reg);
                         mipsBasicBlock.addInstruction(move);
                     }
                 } else {
                     Sw sw = new Sw(reg, 29, offset); //内存的位置存进去
                     offset = offset - 4;
                     mipsBasicBlock.addInstruction(sw);
                 }
             }

             Sw sw = new Sw(31,29,offset); //存入ra
             offset = offset - 4;
             mipsBasicBlock.addInstruction(sw);
             Addi addi = new Addi(29,29,offset); //函数的sp从此开始
             mipsBasicBlock.addInstruction(addi);
             Addi addi1 = new Addi(30,30,fp_offset); //函数的fp
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
             if (params.size() > 3) {
                 offset = offset + (params.size() - 3) * 4;//传参用的是栈，参数没用了
             }
             for (int i = uses.size() - 1;i >= 0;i--) { //恢复
                 offset = offset + 4;
                 Lw lw1 = new Lw(uses.get(i),29,offset);
                 mipsBasicBlock.addInstruction(lw1);
             }

             if (call.getName().length() > 0) { //有返回值需要存储
                 String name = call.getName();
                 if (isAllocated(call)) {
                     int reg = getReg(call);
                     Move move = new Move(reg,2);
                     mipsBasicBlock.addInstruction(move);
                 }  else {
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
                     } else {
                         Sw sw1 = new Sw(2,name);
                         mipsBasicBlock.addInstruction(sw1);
                     }
                 }
             }
         } else {
             System.out.println("check function call when generating mips");
         }
     }

     public void dealMidMove(LlvmIrValue llvmIrValue) {
        MidMove midMove = (MidMove) llvmIrValue;
        LlvmIrValue src = midMove.getSrc();
        LlvmIrValue dst = midMove.getDst();
        int reg1 = 26;
        int reg2 = 27;
        int flag = 0;
        if (isConstant(src.getName())) { //如果是
            flag = 1;
        } else {
            if (isAllocated(src)) {
                reg2 = getReg(src);
            } else {
                findFromMem(src.getName(), reg2);
            }
        }
         if (isAllocated(dst)) {
             reg1 = getReg(dst);
             if (flag == 0) {
                 Move move = new Move(reg1,reg2);
                 mipsBasicBlock.addInstruction(move);
             } else {
                 Li li = new Li(reg1,Integer.parseInt(src.getName()));
                 mipsBasicBlock.addInstruction(li);
             }
         } else {
             int pos;
             if (mipsSymbolTable.containsMipsSymbol(dst.getName())) {
                 MipsSymbol mipsSymbol = mipsSymbolTable.getMipsSymbol(dst.getName());
                 pos = mipsSymbol.getOffset();
             } else {
                 pos = offset;
                 offset = offset - 4;
                 MipsSymbol mipsSymbol = new MipsSymbol(dst.getName(),pos,dst);
                 mipsSymbolTable.addMipsSymbol(dst.getName(), mipsSymbol);
             }
             if (flag != 0) {
                 Li li = new Li(reg2,Integer.parseInt(src.getName()));
                 mipsBasicBlock.addInstruction(li);
             }
             Sw sw = new Sw(reg2, 29, pos);
             mipsBasicBlock.addInstruction(sw);
         }
    }
}

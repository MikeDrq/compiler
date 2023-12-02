package SyntaxTree;

import Lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class StmtNode extends TreeNode {
    private String name = "<Stmt>";
    private int mark;
    private LValNode lValNode;
    private Token assign;
    private ExpNode expNode;
    private Token semicn;
    private Token semicn_for;
    private Token wordIf;
    private Token lparent;
    private CondNode condNode;
    private Token rparent;
    private StmtNode stmtNode;
    private Token wordElse;
    private StmtNode stmtNode_else;
    private Token wordFor;
    private ForStmtNode forStmtNode_1;
    private ForStmtNode forStmtNode2;
    private Token wordBreak;
    private Token wordContinue;
    private BlockNode blockNode;
    private Token wordReturn;
    private Token wordGetint;
    private Token wordPrint;
    private Token formatstring;
    private ArrayList<Token> comma = new ArrayList<>();
    private ArrayList<ExpNode> expNodes = new ArrayList<>();

    public StmtNode() {

    }
    public StmtNode(int mark) {
        this.mark = mark;
    }

    public void addMark(int s) {
        this.mark = s;
    }

    public void addLValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void addAssign(Token assign) {
        this.assign = assign;
    }

    public void addCondNode(CondNode condNode) {
        this.condNode = condNode;
    }

    public void addExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public void addSemicn(Token semicn) {
        this.semicn = semicn;
    }

    public void addWordIf(Token wordIf) {
        this.wordIf = wordIf;
    }

    public void addLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void addRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void addStmtNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }

    public void addWordElse(Token wordElse) {
        this.wordElse = wordElse;
    }

    public void addStmtNode_else(StmtNode stmtNode_else) {
        this.stmtNode_else = stmtNode_else;
    }

    public void addWordFor(Token wordFor) {
        this.wordFor = wordFor;
    }

    public void addForStmtNode_1(ForStmtNode forStmtNode_1) {
        this.forStmtNode_1 = forStmtNode_1;
    }

    public void addForStmtNode2(ForStmtNode forStmtNode2) {
        this.forStmtNode2 = forStmtNode2;
    }

    public void addWordBreak(Token wordBreak) {
        this.wordBreak = wordBreak;
    }

    public void addWordContinue(Token wordContinue) {
        this.wordContinue = wordContinue;
    }

    public void addWordReturn(Token wordReturn) {
        this.wordReturn = wordReturn;
    }

    public void addWordGetint(Token wordGetint) {
        this.wordGetint = wordGetint;
    }

    public void addWordPrint(Token wordPrint) {
        this.wordPrint = wordPrint;
    }

    public void addFormatstring(Token formatstring) {
        this.formatstring = formatstring;
    }

    public void addComma(Token comma) {
        this.comma.add(comma);
    }

    public void addExpNodes(ExpNode expNode) {
        this.expNodes.add(expNode);
    }

    public void addBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public void addSemicn_for(Token semicn_for) {
        this.semicn_for = semicn_for;
    }

    public int getMark() {
        return mark;
    }

    public BlockNode getBlockNode() {
        return this.blockNode;
    }

    public LValNode getlValNode() {
        return this.lValNode;
    }

    public ExpNode getExpNode() {
        return this.expNode;
    }

    public Token getWordGetint() {
        return wordGetint;
    }

    public Token getFormatstring() {
        return this.formatstring;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return this.expNodes;
    }

    public CondNode getCondNode() {
        return this.condNode;
    }

    public StmtNode getStmtNode() {
        return this.stmtNode;
    }

    public StmtNode getStmtNode_else() {
        return  this.stmtNode_else;
    }

    public ForStmtNode getForStmtNode_1() {
        return forStmtNode_1;
    }

    public ForStmtNode getForStmtNode2() {
        return forStmtNode2;
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
    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (mark == 1) {
            sb.append(lValNode.print());
            sb.append(assign.toString());
            sb.append(expNode.print());
            sb.append(semicn.toString());
        } else if (mark == 2) {
            sb.append(expNode.print());
            sb.append(semicn.toString());
        } else if (mark == 3) {
            sb.append(semicn.toString());
        } else if (mark == 4) {
            sb.append(blockNode.print());
        } else if (mark == 5) {
            sb.append(wordIf.toString());
            sb.append(lparent.toString());
            sb.append(condNode.print());
            sb.append(rparent.toString());
            sb.append(stmtNode.print());
            if (wordElse != null) {
                sb.append(wordElse.toString());
                sb.append(stmtNode_else.print());
            }
        } else if (mark == 6) {
            sb.append(wordFor.toString());
            sb.append(lparent.toString());
            if (forStmtNode_1 != null) {
                sb.append(forStmtNode_1.print());
            }
            sb.append(semicn.toString());
            if (condNode != null) {
                sb.append(condNode.print());
            }
            sb.append(semicn_for.toString());
            if (forStmtNode2 != null) {
                sb.append(forStmtNode2.print());
            }
            sb.append(rparent.toString());
            sb.append(stmtNode.print());
        } else if (mark == 7) {
            sb.append(wordBreak.toString()).append(semicn.toString());
        } else if (mark == 8) {
            sb.append(wordContinue.toString()).append(semicn.toString());
        } else if (mark == 9) {
            sb.append(wordReturn.toString());
            if (expNode != null) {
                sb.append(expNode.print());
            }
            sb.append(semicn.toString());
        } else if (mark == 10) {
            sb.append(lValNode.print());
            sb.append(assign.toString());
            sb.append(wordGetint.toString());
            sb.append(lparent.toString());
            sb.append(rparent.toString());
            sb.append(semicn.toString());
        } else {
            sb.append(wordPrint.toString());
            sb.append(lparent.toString());
            sb.append(formatstring.toString());
            if (comma.size() > 0) {
                for(int i = 0;i < comma.size();i++) {
                    sb.append(comma.get(i).toString());
                    sb.append(expNodes.get(i).print());
                }
            }
            sb.append(rparent.toString());
            sb.append(semicn.toString());
        }
        sb.append(name).append("\n");
        return sb.toString();
    }
}

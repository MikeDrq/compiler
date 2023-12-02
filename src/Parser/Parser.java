package Parser;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.*;
import SyntaxTree.*;
import Error.Error;
import Error.ErrorTable;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokenList;
    private int curPos;
    private Token curToken;
    private ArrayList<String> grammarList;
    private SymbolTable symbolTable;
    private SymbolTable curSymbolTable;
    private ArrayList<Symbol> paramSymbols;
    private ErrorTable errorTable;
    private int loopDepth = 0;
    private boolean actReturn = false;
    private int returnLine;
    private int rBlockLine;
    private boolean testError = false;

    public Parser(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
        this.curPos = -1;
        this.grammarList = new ArrayList<>();
        this.symbolTable = new SymbolTable(null);
        this.curSymbolTable = symbolTable;
        this.errorTable = new ErrorTable();
    }

    private void getToken() {
        curPos++;
        if (curPos >= tokenList.size()) {
            curToken = null;
        } else {
            curToken = tokenList.get(curPos);
        }
    }

    private Token getNextToken(int k) {
        if (curPos + k >= tokenList.size()) {
            return null;
        }
        return tokenList.get(curPos + k);
    }

    public SymbolTable getSymbolTable() {
        if (curSymbolTable == symbolTable) {
            System.out.println("1");
        } else {
            System.out.println("0");
        }
        return symbolTable;
    }

    public ErrorTable getErrorTable() {
        return this.errorTable;
    }

    public CompUnitNode parseCompUnit()  {
        CompUnitNode compUnitNode = new CompUnitNode();
        getToken();
        while (getNextToken(1) != null && getNextToken(1).getTokenType() != TokenType.MAINTK) {
            Token tokenTwo = getNextToken(2);
            if (tokenTwo != null && (tokenTwo.getTokenType() != TokenType.LPARENT)) {
                compUnitNode.addDeclNode(parseDecl());
            } else {
                compUnitNode.addFuncDefNode(parseFuncDef());
            }
        }
        compUnitNode.addMainFuncDefNode(parseMainFuncDef());
        grammarList.add("<CompUnit>\n");
        //return grammarList;
        return compUnitNode;
    }

    public DeclNode parseDecl()  {
        DeclNode declNode;
        if (curToken != null && curToken.getTokenType() == TokenType.INTTK) {
            declNode = new DeclNode(parseVarDecl());
            return declNode;
        } else if (curToken != null && curToken.getTokenType() == TokenType.CONSTTK) {
            declNode = new DeclNode(parseConstDecl());
            return declNode;
        } else {/*error*/return null;}
    }

    public ConstDeclNode parseConstDecl()  {
        grammarList.add(curToken.toString());//const
        Token cons = curToken;
        getToken(); //int
        BTypeNode bTypeNode = new BTypeNode(curToken);
        ConstDeclNode constDeclNode = new ConstDeclNode(cons,bTypeNode);
        grammarList.add(curToken.toString());
        getToken();
        constDeclNode.add(parseConstDef());
        while (curToken.getTokenType() == TokenType.COMMA) {
            grammarList.add(curToken.toString());
            constDeclNode.addToken(curToken);
            getToken();
            constDeclNode.add(parseConstDef());
        }
        if (curToken.getTokenType() != TokenType.SEMICN) {
            grammarList.add(";");
            constDeclNode.addSemicn(new Token(";",tokenList.get(curPos - 1).getLine(),0,TokenType.SEMICN)); //补一个分号
            errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"i"));
        } else {
            grammarList.add(curToken.toString());
            constDeclNode.addSemicn(curToken);
            getToken();
        }
        grammarList.add("<ConstDecl>\n");
        return constDeclNode;
    }

    public ConstDefNode parseConstDef()  {
        grammarList.add(curToken.toString());
        ConstDefNode constDefNode = new ConstDefNode(curToken);
        Token ident = curToken;
        getToken();
        int dim = 0;
        while (curToken.getTokenType() == TokenType.LBRACK) {
            grammarList.add(curToken.toString());
            constDefNode.addLbrack(curToken);
            getToken();
            constDefNode.addConstExpNode(parseConstExp());
            if (curToken.getTokenType() == TokenType.RBRACK) {
                grammarList.add(curToken.toString());
                constDefNode.addRbrack(curToken);
                getToken(); //[ or =
            } else {
                grammarList.add("]");
                constDefNode.addRbrack(new Token("]",tokenList.get(curPos - 1).getLine(),0,TokenType.RBRACK));
                errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"k"));
            }
            dim++;
        }
        grammarList.add(curToken.toString());
        constDefNode.addAssign(curToken);
        getToken();
        constDefNode.addConstInitValNode(parseConstInitVal());
        grammarList.add("<ConstDef>\n");
        addConstSymbol(ident,dim);
        return constDefNode;
    }

    public ConstInitValNode parseConstInitVal()  {
        ConstInitValNode constInitValNode = new ConstInitValNode();
        if (curToken.getTokenType() == TokenType.LBRACE) {
            grammarList.add(curToken.toString());
            constInitValNode.addLbrace(curToken);
            getToken();
            if (curToken.getTokenType() != TokenType.RBRACE) {
                constInitValNode.addConstInitValNode(parseConstInitVal());
                while (curToken.getTokenType() == TokenType.COMMA) {
                    grammarList.add(curToken.toString());
                    constInitValNode.addComma(curToken);
                    getToken();
                    constInitValNode.addConstInitValNode(parseConstInitVal());
                }
                grammarList.add(curToken.toString());
                constInitValNode.addRbrace(curToken);
                getToken();
            } else {
                grammarList.add(curToken.toString());
                constInitValNode.addRbrace(curToken);
                getToken();
            }
        } else {
            constInitValNode.addConstExpNode(parseConstExp());
        }
        grammarList.add("<ConstInitVal>\n");
        return constInitValNode;
    }

    public VarDeclNode parseVarDecl()  {
        grammarList.add(curToken.toString());
        BTypeNode bTypeNode = new BTypeNode(curToken);
        VarDeclNode varDeclNode = new VarDeclNode(bTypeNode);
        getToken();
        varDeclNode.addVarDefNode(parseVarDef());
        while (curToken.getTokenType() == TokenType.COMMA) {
            grammarList.add(curToken.toString());
            varDeclNode.addComma(curToken);
            getToken();
            varDeclNode.addVarDefNode(parseVarDef());
        }
        if (curToken.getTokenType() != TokenType.SEMICN) {
            grammarList.add(";");
            varDeclNode.addSemicn(new Token(";",tokenList.get(curPos - 1).getLine(),0,TokenType.SEMICN)); //补一个分号
            errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"i"));
        } else {
            grammarList.add(curToken.toString());
            varDeclNode.addSemicn(curToken);
            getToken();
        }
        grammarList.add("<VarDecl>\n");
        return varDeclNode;
    }

    public VarDefNode parseVarDef()  {
        grammarList.add(curToken.toString());
        VarDefNode varDefNode = new VarDefNode(curToken);
        Token ident = curToken;
        int dim = 0;
        getToken();
        while (curToken.getTokenType() == TokenType.LBRACK) {
            grammarList.add(curToken.toString());
            varDefNode.addLbrack(curToken);
            getToken();
            varDefNode.addConstExpNode(parseConstExp());
            if (curToken.getTokenType() == TokenType.RBRACK) {
                grammarList.add(curToken.toString());
                varDefNode.addRbrack(curToken);
                getToken();
            } else {
                grammarList.add("]");
                varDefNode.addRbrack(new Token("]",tokenList.get(curPos - 1).getLine(),0,TokenType.RBRACK));
                errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"k"));
            }
            dim++;
        }
        if (curToken.getTokenType() == TokenType.ASSIGN) {
            grammarList.add(curToken.toString());
            varDefNode.addAsign(curToken);
            getToken();
            varDefNode.addInitValNode(parseInitVal());
        }
        addVarSymbol(ident,dim);
        grammarList.add("<VarDef>\n");
        return varDefNode;
    }

    public InitValNode parseInitVal()  {
        InitValNode initValNode;
        if (curToken.getTokenType() == TokenType.LBRACE) {
            initValNode = new InitValNode();
            grammarList.add(curToken.toString());
            initValNode.addLbrace(curToken);
            getToken();
            if (curToken.getTokenType() != TokenType.RBRACE) {
                initValNode.addInitValNode(parseInitVal());
                while (curToken.getTokenType() == TokenType.COMMA) {
                    grammarList.add(curToken.toString());
                    initValNode.addComma(curToken);
                    getToken();
                    initValNode.addInitValNode(parseInitVal());
                }
                grammarList.add(curToken.toString());
                initValNode.addRbrace(curToken);
                getToken();
            } else {
                grammarList.add(curToken.toString());
                initValNode.addLbrace(curToken);
                getToken();
            }
        } else {
            initValNode = new InitValNode(parseExp());
        }
        grammarList.add("<InitVal>\n");
        return initValNode;
    }

    public FuncDefNode parseFuncDef()  {
        grammarList.add(curToken.toString());
        grammarList.add("<FuncType>\n");
        FuncTypeNode funcTypeNode = new FuncTypeNode(curToken);
        boolean isVoid;
        if (curToken.getTokenType() == TokenType.INTTK) {
            isVoid = false;
        } else {
            isVoid = true;
        }
        getToken();
        grammarList.add(curToken.toString());
        FuncDefNode funcDefNode = new FuncDefNode(funcTypeNode,curToken);
        Token ident = curToken;
        Symbol funcSymbol = addFuncSymbol(ident,isVoid);
        createNewTable();
        this.paramSymbols = new ArrayList<>();
        getToken(); //(
        grammarList.add(curToken.toString());
        funcDefNode.addLparent(curToken);
        getToken();
        boolean noparams = false;
        if (curToken.getTokenType() == TokenType.LBRACE) {
            grammarList.add(")");
            funcDefNode.addRparent(new Token(")",tokenList.get(curPos - 1).getLine(),0,TokenType.RPARENT));
            errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"j"));
            noparams = true;
        } else if (curToken.getTokenType() != TokenType.RPARENT) {
            funcDefNode.addFuncFParamsNode(parseFuncFParmas());
        }
        if (funcSymbol != null) {
            funcSymbol.setParams(paramSymbols);
            for (Symbol symbol : paramSymbols) {
                if (!curSymbolTable.checkBTypeError(symbol)) {
                    curSymbolTable.addItem(symbol);
                } else {
                    errorTable.addError(new Error(symbol.getLine(),"b"));
                }
            }
        }
        paramSymbols = new ArrayList<>();
        if (curToken.getTokenType() == TokenType.RPARENT) {
            grammarList.add(curToken.toString());
            funcDefNode.addRparent(curToken);
            getToken();
        } else {
            if (!noparams) {
                grammarList.add(")");
                funcDefNode.addRparent(new Token(")", tokenList.get(curPos - 1).getLine(), 0, TokenType.RPARENT));
                errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"j"));
            }
        }
        funcDefNode.addBlockNode(parseBlock());
        grammarList.add("<FuncDef>\n");
        curSymbolTable = curSymbolTable.getParent();
        if (isVoid) {
            if(actReturn) {
                errorTable.addError(new Error(returnLine,"f"));
            }
        } else {
            if (!actReturn) {
                errorTable.addError(new Error(rBlockLine,"g"));
            }
        }
        actReturn = false;
        return funcDefNode;
    }

    public MainFuncDefNode parseMainFuncDef()  {
        MainFuncDefNode mainFuncDefNode = new MainFuncDefNode();
        grammarList.add(curToken.toString());
        mainFuncDefNode.addWordInt(curToken);
        getToken();
        grammarList.add(curToken.toString());
        mainFuncDefNode.addWordMain(curToken);
        Token ident = curToken;
        addFuncSymbol(ident,false);
        createNewTable();
        getToken();
        grammarList.add(curToken.toString());
        mainFuncDefNode.addLparent(curToken);
        getToken();
        grammarList.add(curToken.toString());
        mainFuncDefNode.addRparent(curToken);
        getToken();
        mainFuncDefNode.addBlockNode(parseBlock());
        grammarList.add("<MainFuncDef>\n");
        curSymbolTable = curSymbolTable.getParent();
        if (!actReturn) {
            errorTable.addError(new Error(rBlockLine,"g"));
        }
        return mainFuncDefNode;
    }

    public FuncFParamsNode parseFuncFParmas()  {
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode();
        funcFParamsNode.addFuncFParamNode(parseFuncFParma());
        while (curToken.getTokenType() == TokenType.COMMA) {
            grammarList.add(curToken.toString());
            funcFParamsNode.addComma(curToken);
            getToken();
            funcFParamsNode.addFuncFParamNode(parseFuncFParma());
        }
        grammarList.add("<FuncFParams>\n");
        return funcFParamsNode;
    }

    public FuncFParamNode parseFuncFParma()  {
        grammarList.add(curToken.toString());
        BTypeNode bTypeNode = new BTypeNode(curToken);
        FuncFParamNode funcFParamNode = new FuncFParamNode(bTypeNode);
        getToken();
        grammarList.add(curToken.toString());
        funcFParamNode.addIdent(curToken);
        Token ident = curToken;
        getToken();
        int dim = 0;
        if (curToken.getTokenType() == TokenType.LBRACK) {
            grammarList.add(curToken.toString());
            funcFParamNode.addFlparent(curToken);
            getToken();
            if (curToken.getTokenType() == TokenType.RBRACK) {
                grammarList.add(curToken.toString());
                funcFParamNode.addFrparent(curToken);
                getToken();
            } else {
                grammarList.add("]");
                funcFParamNode.addFrparent(new Token("]",tokenList.get(curPos - 1).getLine(),0,TokenType.RBRACK));
                errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"k"));
            }
            dim++;
            while (curToken.getTokenType() == TokenType.LBRACK) {
                grammarList.add(curToken.toString());
                funcFParamNode.addLparent(curToken);
                getToken();
                funcFParamNode.addConstExpNode(parseConstExp());
                if (curToken.getTokenType() == TokenType.RBRACK) {
                    grammarList.add(curToken.toString());
                    funcFParamNode.addRparent(curToken);
                    getToken();
                } else {
                    grammarList.add("]");
                    funcFParamNode.addRparent(new Token("]",tokenList.get(curPos - 1).getLine(),0,TokenType.RBRACK));
                    errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"k"));
                }
                dim++;
            }
        }
        Symbol symbol;
        if (dim == 0) {
            symbol = new Symbol(ident,SymbolType.VAR);
        } else if (dim == 1) {
            symbol = new Symbol(ident,SymbolType.VAR_ARRAY1);
        } else if (dim == 2) {
            symbol = new Symbol(ident,SymbolType.VAR_ARRAY2);
        } else {
            symbol = new Symbol(ident,null);
        }
        symbol.addDimension(dim);
        paramSymbols.add(symbol);
        grammarList.add("<FuncFParam>\n");
        return funcFParamNode;
    }

    public BlockNode parseBlock()  {
        BlockNode blockNode = new BlockNode();
        grammarList.add(curToken.toString());
        blockNode.addLbrace(curToken);
        getToken();
        while (curToken.getTokenType() != TokenType.RBRACE) {
            blockNode.addBlockItemNode(parseBlockItem());
        }
        grammarList.add(curToken.toString());
        blockNode.addRbrace(curToken);
        rBlockLine = curToken.getLine();
        grammarList.add("<Block>\n");
        getToken();
        return blockNode;
    }

    public BlockItemNode parseBlockItem()  {
        BlockItemNode blockItemNode;
        if (curToken.getTokenType() == TokenType.CONSTTK || curToken.getTokenType() == TokenType.INTTK) {
            blockItemNode = new BlockItemNode(parseDecl());
        } else {
            blockItemNode = new BlockItemNode(parseStmt());
        }
        return blockItemNode;
    }

    public StmtNode parseStmt()  {
        StmtNode stmtNode;
        if (curToken.getTokenType() == TokenType.IFTK) { //if
            stmtNode = new StmtNode(5);
            grammarList.add(curToken.toString());
            stmtNode.addWordIf(curToken);
            getToken();
            grammarList.add(curToken.toString());
            stmtNode.addLparent(curToken);
            getToken();
            stmtNode.addCondNode(parseCond());
            /*grammarList.add(curToken.toString());
            stmtNode.addRparent(curToken);
            getToken();*/
            checkStmtRparent(stmtNode);
            stmtNode.addStmtNode(parseStmt());
            if (curToken.getTokenType() == TokenType.ELSETK) {
                grammarList.add(curToken.toString());
                stmtNode.addWordElse(curToken);
                getToken();
                stmtNode.addStmtNode_else(parseStmt());
            }
        } else if (curToken.getTokenType() == TokenType.FORTK) { //for
            stmtNode = new StmtNode(6);
            grammarList.add(curToken.toString());
            stmtNode.addWordFor(curToken);
            getToken();
            grammarList.add(curToken.toString());
            stmtNode.addLparent(curToken);
            getToken();
            if (curToken.getTokenType() != TokenType.SEMICN) {
                stmtNode.addForStmtNode_1(parseForStmt());
            }
            grammarList.add(curToken.toString());
            stmtNode.addSemicn(curToken);
            getToken();
            if (curToken.getTokenType() != TokenType.SEMICN) {
                stmtNode.addCondNode(parseCond());
            }
            grammarList.add(curToken.toString());
            stmtNode.addSemicn_for(curToken);
            getToken();
            if (curToken.getTokenType() != TokenType.RPARENT){
                stmtNode.addForStmtNode2(parseForStmt());
            }
            /*grammarList.add(curToken.toString());
            stmtNode.addRparent(curToken);
            getToken();*/
            checkStmtRparent(stmtNode);
            loopDepth++;
            stmtNode.addStmtNode(parseStmt());
            loopDepth--;
        } else if (curToken.getTokenType() == TokenType.BREAKTK || curToken.getTokenType() == TokenType.CONTINUETK) {
            if (curToken.getTokenType() == TokenType.BREAKTK) {
                stmtNode = new StmtNode(7);
                stmtNode.addWordBreak(curToken);
            } else {
                stmtNode = new StmtNode(8);
                stmtNode.addWordContinue(curToken);
            }
            if (loopDepth == 0) {
                errorTable.addError(new Error(curToken.getLine(), "m"));
            }
            grammarList.add(curToken.toString());
            getToken();
            checkStmtSemicn(stmtNode);
        } else if (curToken.getTokenType() == TokenType.PRINTFTK) {
            stmtNode = new StmtNode(11);
            grammarList.add(curToken.toString());
            stmtNode.addWordPrint(curToken);
            int lineNumber = curToken.getLine();
            getToken();
            grammarList.add(curToken.toString());
            stmtNode.addLparent(curToken);
            getToken();
            grammarList.add(curToken.toString());
            stmtNode.addFormatstring(curToken);
            checkFormatstring(curToken);
            String string = curToken.getToken();
            getToken();
            int params = 0;
            while (curToken.getTokenType() == TokenType.COMMA) {
                grammarList.add(curToken.toString());
                stmtNode.addComma(curToken);
                getToken();
                stmtNode.addExpNodes(parseExp());
                params++;
            }
            /*grammarList.add(curToken.toString()); //)
            stmtNode.addRparent(curToken);
            getToken();*/
            checkStmtRparent(stmtNode);

            int count =0;
            int index = string.indexOf("%d");
            while(index != -1) {
                count++;
                index = string.indexOf("%d",index + 1);
            }
            if (count != params) {
                errorTable.addError(new Error(lineNumber,"l"));
            }
            checkStmtSemicn(stmtNode);
        } else if (curToken.getTokenType() == TokenType.SEMICN) {
            stmtNode = new StmtNode(3);
            grammarList.add(curToken.toString());
            stmtNode.addSemicn(curToken);
            getToken();
        } else if (curToken.getTokenType() == TokenType.LBRACE) {
            stmtNode = new StmtNode(4);
            createNewTable();
            stmtNode.addBlockNode(parseBlock());
            curSymbolTable = curSymbolTable.getParent();
        } else if (curToken.getTokenType() == TokenType.RETURNTK) {
            actReturn = false;
            returnLine = curToken.getLine();
            stmtNode = new StmtNode(9);
            grammarList.add(curToken.toString());
            stmtNode.addWordReturn(curToken);

            getToken();
            if (curToken.getTokenType() != TokenType.SEMICN && curToken.getTokenType() != TokenType.RBRACE) {
                stmtNode.addExpNode(parseExp());
                actReturn = true;
            }
            checkStmtSemicn(stmtNode);
        } else {
            int k = 0;
            int flag = 0;
            while (getNextToken(k) != null && getNextToken(k).getTokenType() != TokenType.SEMICN) {
                if (getNextToken(k).getTokenType() == TokenType.ASSIGN) {
                    flag = 1;
                    break;
                }
                k++;
            }

            if (flag == 1) {
                stmtNode = new StmtNode();
                LValNode lValNode = parseLVal();
                stmtNode.addLValNode(lValNode);
                grammarList.add(curToken.toString()); //=
                stmtNode.addAssign(curToken);
                getToken();
                Token t = lValNode.getIdent();
                Symbol symbol = curSymbolTable.hasVar(t);
                if (symbol != null) {
                    if (symbol.getType() == SymbolType.CONST || symbol.getType() == SymbolType.CONST_ARRAY1 || symbol.getType() == SymbolType.CONST_ARRAY2) {
                        errorTable.addError(new Error(t.getLine(), "h"));
                    }
                }
                if (curToken.getTokenType() == TokenType.GETINTTK) {
                    stmtNode.addMark(10);
                    grammarList.add(curToken.toString()); //getInt
                    stmtNode.addWordGetint(curToken);
                    getToken();
                    grammarList.add(curToken.toString()); //(
                    stmtNode.addLparent(curToken);
                    getToken();
                    /*grammarList.add(curToken.toString()); //)
                    stmtNode.addRparent(curToken);
                    getToken();*/
                    checkStmtRparent(stmtNode);
                    /*grammarList.add(curToken.toString());
                    stmtNode.addSemicn(curToken);
                    getToken();*/
                    checkStmtSemicn(stmtNode);
                } else {
                    stmtNode.addMark(1);
                    stmtNode.addExpNode(parseExp());
                    checkStmtSemicn(stmtNode);
                }
            } else {
                stmtNode = new StmtNode(2);
                stmtNode.addExpNode(parseExp());
                checkStmtSemicn(stmtNode);
            }
        }
        grammarList.add("<Stmt>\n");
        return stmtNode;
    }

    public ForStmtNode parseForStmt()  {
        ForStmtNode forStmtNode = new ForStmtNode();
        forStmtNode.addlValNode(parseLVal());
        grammarList.add(curToken.toString());
        forStmtNode.addAssign(curToken);
        getToken();
        forStmtNode.addExpNode(parseExp());
        grammarList.add("<ForStmt>\n");
        return forStmtNode;
    }

    public ExpNode parseExp()  {
        ExpNode expNode = new ExpNode(parseAddExp());
        grammarList.add("<Exp>\n");
        return expNode;
    }

    public CondNode parseCond()  {
        CondNode condNode = new CondNode(parseLOrExp());
        grammarList.add("<Cond>\n");
        return condNode;
    }

    public LValNode parseLVal()  {
        LValNode lValNode = new LValNode();
        grammarList.add(curToken.toString());
        lValNode.addIdent(curToken);
        if (!curSymbolTable.checkCTypeError(curToken,0)) {
            errorTable.addError(new Error(curToken.getLine(),"c"));
        }
        getToken();
        while(curToken.getTokenType() == TokenType.LBRACK) {
            grammarList.add(curToken.toString());
            lValNode.addLbrack(curToken);
            getToken();
            lValNode.addExpNode(parseExp());
            if (curToken.getTokenType() == TokenType.RBRACK) {
                grammarList.add(curToken.toString());
                lValNode.addRbrack(curToken);
                getToken();
            } else {
                grammarList.add("]");
                lValNode.addRbrack(new Token("]",tokenList.get(curPos - 1).getLine(),0,TokenType.RBRACK));
                errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"k"));
            }
        }
        grammarList.add("<LVal>\n");
        return lValNode;
    }

    public PrimaryExpNode parsePrimaryExp() {
        PrimaryExpNode primaryExpNode = new PrimaryExpNode();
        if (curToken.getTokenType() == TokenType.LPARENT) {
            grammarList.add(curToken.toString());
            primaryExpNode.addLparent(curToken);
            getToken();
            primaryExpNode.addExpNode(parseExp());
            grammarList.add(curToken.toString());
            primaryExpNode.addRparent(curToken);
            getToken();
        } else if (curToken.getTokenType() == TokenType.IDENFR) {
            primaryExpNode.addlValNode(parseLVal());
        } else if (curToken.getTokenType() == TokenType.INTCON) {
            primaryExpNode.addNumberNode(parseNumber());
        } else {/*error*/}
        grammarList.add("<PrimaryExp>\n");
        return primaryExpNode;
    }

    public NumberNode parseNumber() {
        grammarList.add(curToken.toString());
        NumberNode numberNode = new NumberNode(curToken);
        grammarList.add("<Number>\n");
        getToken();
        return numberNode;
    }

    public UnaryExpNode parseUnaryExp()  {
        UnaryExpNode unaryExpNode = new UnaryExpNode();
        if (curToken.getTokenType() == TokenType.LPARENT || curToken.getTokenType() == TokenType.INTCON) {
            unaryExpNode.addPrimaryExpNode(parsePrimaryExp());
        } else if (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU || curToken.getTokenType() == TokenType.NOT) {
            grammarList.add(curToken.toString());
            UnaryOpNode unaryOpNode = new UnaryOpNode(curToken);
            unaryExpNode.addUnaryOpNode(unaryOpNode);
            grammarList.add("<UnaryOp>\n");
            getToken();
            unaryExpNode.addUnaryExpNode(parseUnaryExp());
        } else if (curToken.getTokenType() == TokenType.IDENFR) { //函数
            if (getNextToken(1) != null && getNextToken(1).getTokenType() != TokenType.LPARENT) {
                unaryExpNode.addPrimaryExpNode(parsePrimaryExp());
            } else {
                ArrayList<Symbol> paramsList = null;
                if (!curSymbolTable.checkCTypeError(curToken,1)) {
                    errorTable.addError(new Error(curToken.getLine(),"c"));
                } else {
                    paramsList = curSymbolTable.getFParamList(curToken);
                }
                int funcLine = curToken.getLine();
                grammarList.add(curToken.toString());
                unaryExpNode.addIdent(curToken);
                getToken();
                grammarList.add(curToken.toString());
                unaryExpNode.addLparent(curToken);
                getToken();
                int rParams = 0;
                FuncRParamsNode funcRParamsNode = null;
                /*if (curToken.getTokenType() != TokenType.RPARENT) {
                    funcRParamsNode = parseFuncRParams();
                    unaryExpNode.addFuncFParamsNodes(funcRParamsNode);
                    rParams = funcRParamsNode.getParamsNumber();
                    grammarList.add(curToken.toString());
                    unaryExpNode.addRparent(curToken);
                    getToken();
                } else { //无参数
                    grammarList.add(curToken.toString());
                    unaryExpNode.addRparent(curToken);
                    getToken();
                }*/
                if (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU || curToken.getTokenType() == TokenType.NOT
                 || curToken.getTokenType() == TokenType.IDENFR || curToken.getTokenType() == TokenType.LPARENT || curToken.getTokenType() == TokenType.INTCON) {
                    funcRParamsNode = parseFuncRParams();
                    unaryExpNode.addFuncFParamsNodes(funcRParamsNode);
                    rParams = funcRParamsNode.getParamsNumber();
                }
                if (curToken.getTokenType() == TokenType.RPARENT) {
                    grammarList.add(curToken.toString());
                    unaryExpNode.addRparent(curToken);
                    getToken();
                    curSymbolTable.checkFuncRParam(paramsList, rParams, funcLine, errorTable, funcRParamsNode, curSymbolTable);
                } else {
                    grammarList.add(")");
                    unaryExpNode.addRparent(new Token(")",tokenList.get(curPos - 1).getLine(),0,TokenType.RPARENT));
                    errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"j"));
                }
            }
        } else {/*error*/}
        grammarList.add("<UnaryExp>\n");
        return unaryExpNode;
    }

    public FuncRParamsNode parseFuncRParams()  {
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode();
        funcRParamsNode.addExpNodes(parseExp());
        while (curToken.getTokenType() == TokenType.COMMA) {
            grammarList.add(curToken.toString());
            funcRParamsNode.addComma(curToken);
            getToken();
            funcRParamsNode.addExpNodes(parseExp());
        }
        grammarList.add("<FuncRParams>\n");
        return funcRParamsNode;
    }

    public MulExpNode parseMulExp()  {
        MulExpNode mulExpNode = new MulExpNode();
        mulExpNode.addUnaryExpNode(parseUnaryExp());
        grammarList.add("<MulExp>\n");
        while (curToken.getTokenType() == TokenType.MULT || curToken.getTokenType() == TokenType.DIV || curToken.getTokenType() == TokenType.MOD) {
            grammarList.add(curToken.toString());
            mulExpNode.addOp(curToken);
            getToken();
            mulExpNode.addUnaryExpNode(parseUnaryExp());
            grammarList.add("<MulExp>\n");
        }
        return mulExpNode;
    }

    public AddExpNode parseAddExp()  {
        AddExpNode addExpNode = new AddExpNode();
        addExpNode.addAddExpNode(parseMulExp());
        grammarList.add("<AddExp>\n");
        while (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU) {
            grammarList.add(curToken.toString());
            addExpNode.addOp(curToken);
            getToken();
            addExpNode.addAddExpNode(parseMulExp());
            grammarList.add("<AddExp>\n");
        }
        return addExpNode;
    }

    public RelExpNode parseRelExp()  {
        RelExpNode relExpNode = new RelExpNode();
        relExpNode.addAddExpNode(parseAddExp());
        grammarList.add("<RelExp>\n");
        while (curToken.getTokenType() == TokenType.LSS || curToken.getTokenType() == TokenType.LEQ || curToken.getTokenType() == TokenType.GRE || curToken.getTokenType() == TokenType.GEQ) {
            grammarList.add(curToken.toString());
            relExpNode.addOp(curToken);
            getToken();
            relExpNode.addAddExpNode(parseAddExp());
            grammarList.add("<RelExp>\n");
        }
        return relExpNode;
    }

    public EqExpNode parseEqExp()  {
        EqExpNode eqExpNode = new EqExpNode();
        eqExpNode.addRelExpNode(parseRelExp());
        grammarList.add("<EqExp>\n");
        while (curToken.getTokenType() == TokenType.EQL || curToken.getTokenType() == TokenType.NEQ) {
            grammarList.add(curToken.toString());
            eqExpNode.addOp(curToken);
            getToken();
            eqExpNode.addRelExpNode(parseRelExp());
            grammarList.add("<EqExp>\n");
        }
        return eqExpNode;
    }

    public LAndExpNode parseLAndExp()  {
        LAndExpNode lAndExpNode = new LAndExpNode();
        lAndExpNode.addEqExpNode(parseEqExp());
        grammarList.add("<LAndExp>\n");
        while (curToken.getTokenType() == TokenType.AND) {
            grammarList.add(curToken.toString());
            lAndExpNode.addOp(curToken);
            getToken();
            lAndExpNode.addEqExpNode(parseEqExp());
            grammarList.add("<LAndExp>\n");
        }
        return lAndExpNode;
    }

    public LOrExpNode parseLOrExp()  {
        LOrExpNode lOrExpNode = new LOrExpNode();
        lOrExpNode.addLAndExpNode(parseLAndExp());
        grammarList.add("<LOrExp>\n");
        while (curToken.getTokenType() == TokenType.OR) {
            grammarList.add(curToken.toString());
            lOrExpNode.addOp(curToken);
            getToken();
            lOrExpNode.addLAndExpNode(parseLAndExp());
            grammarList.add("<LOrExp>\n");
        }
        return lOrExpNode;
    }

    public ConstExpNode parseConstExp()  {
        ConstExpNode constExpNode = new ConstExpNode();
        constExpNode.addConstExp(parseAddExp());
        grammarList.add("<ConstExp>\n");
        return constExpNode;
    }

    public void addConstSymbol(Token ident,Integer dimension) { //填Const变量
        SymbolType symbolType;
        int dim = dimension;
        if (dim == 0) {
            symbolType = SymbolType.CONST;
        } else if (dim == 1) {
            symbolType = SymbolType.CONST_ARRAY1;
        } else if (dim == 2){
            symbolType = SymbolType.CONST_ARRAY2;
        } else {
            symbolType = null;
            dim = -1;
        }
        Symbol symbol = new Symbol(ident,symbolType);
        symbol.addDimension(dim);
        if (!curSymbolTable.checkBTypeError(symbol)) {
            curSymbolTable.addItem(symbol);
        } else {
            errorTable.addError(new Error(ident.getLine(),"b"));
        }
    }

    public void addVarSymbol(Token ident,Integer dimension) { //填变量
        SymbolType symbolType;
        int dim = dimension;
        if (dim == 0) {
            symbolType = SymbolType.VAR;
        } else if (dim == 1) {
            symbolType = SymbolType.VAR_ARRAY1;
        } else if (dim == 2){
            symbolType = SymbolType.VAR_ARRAY2;
        } else {
            symbolType = null;
            dim = -1;
        }
        Symbol symbol = new Symbol(ident,symbolType);
        symbol.addDimension(dim);
        if (!curSymbolTable.checkBTypeError(symbol)) {
            curSymbolTable.addItem(symbol);
        } else {
            errorTable.addError(new Error(ident.getLine(),"b"));
        }
    }

    public Symbol addFuncSymbol(Token ident,Boolean isVoid) { //填函数
        Symbol symbol = new Symbol(ident,SymbolType.FUNC);
        symbol.setIsVoid(isVoid);
        if (!curSymbolTable.checkBTypeError(symbol)) {
            curSymbolTable.addItem(symbol);
        } else {
            errorTable.addError(new Error(ident.getLine(),"b"));
            return null;
        }
        return symbol; //为了填入参数
    }

    public void createNewTable() {
        this.curSymbolTable = new SymbolTable(curSymbolTable);
        curSymbolTable.getParent().addChild(curSymbolTable);
    }

    public void checkFormatstring(Token token) {
        String value = token.getToken();
        int len = value.length();
        value = value.substring(1,len - 1);
        int lineNumber = token.getLine();
        boolean right = true;
        for (int i = 0;i < value.length();i++) {
            char c = value.charAt(i);
            if (c < 32 || (c > 33 && c <40) || c > 126) {
                if (c == '%') {
                    if (value.charAt(i + 1) != 'd') {
                        right = false;
                        break;
                    }
                } else {
                    right = false;
                    break;
                }
            }
            if (c == 92) {
                if (i == value.length() - 1) {
                    right = false;
                    break;
                } else {
                    if (value.charAt(i + 1) != 'n') {
                        right = false;
                        break;
                    }
                }
            }
        }
        if (!right) {
            errorTable.addError(new Error(lineNumber,"a"));
        }
    }

    public void checkStmtSemicn(StmtNode stmtNode) {
        if (curToken.getTokenType() != TokenType.SEMICN) {
            grammarList.add(";");
            stmtNode.addSemicn(new Token(";",tokenList.get(curPos - 1).getLine(),0,TokenType.SEMICN)); //补一个分号
            errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"i"));
        } else {
            grammarList.add(curToken.toString());
            stmtNode.addSemicn(curToken);
            getToken();
        }
    }

    public void checkStmtRparent(StmtNode stmtNode) {
        if (curToken.getTokenType() != TokenType.RPARENT) {
            grammarList.add(")");
            stmtNode.addRparent(new Token(")",tokenList.get(curPos - 1).getLine(),0,TokenType.RPARENT));
            errorTable.addError(new Error(tokenList.get(curPos - 1).getLine(),"j"));
        } else {
            grammarList.add(curToken.toString());
            stmtNode.addRparent(curToken);
            getToken();
        }
    }
}
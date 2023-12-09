import Error.ErrorTable;
import Lexer.Lexer;

import java.io.*;
import java.util.ArrayList;
import Lexer.Token;
import Opt.MemToReg;
import Middle.LlvmIr;
import Middle.LlvmIrModule;
import Mips.Mips;
import Mips.MipsModule;
import Opt.Opt;
import Parser.Parser;
import Error.Error;
import SyntaxTree.CompUnitNode;


public class Compiler {
    public static void main(String[] args) {
        StringBuilder content = new StringBuilder();
        String line;
        ArrayList<Token> tokenList;
        ArrayList<String> grammarList;
        String input = "testfile.txt";
        String error_output = "error.txt";
        String mid_output_origin = "llvm_ir_origin.txt";
        String mid_output = "llvm_ir.txt";
        String mips_output = "mips.txt";
        boolean testError = true;
        boolean testMid = true;
        boolean testMips = true;
        boolean hasError = false;
        boolean isCompetition = true;
        try(BufferedReader br = new BufferedReader(new FileReader(input))) {
            while((line = br.readLine()) != null) {
                content.append(line).append('\n');
            }
            if (content.length() > 0) {
                content.deleteCharAt(content.length() - 1);
            }
        } catch (IOException e) {
            System.out.println("testfile.txt 读取失败");
        }
        Lexer lexer = new Lexer(content.toString());
        tokenList = lexer.analysis();

        Parser parser = new Parser(tokenList);
        CompUnitNode compUnitNode = parser.parseCompUnit();
        //writer.write(compUnitNode.print());
        if (testError) {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(error_output))) {
                ErrorTable errorTable = parser.getErrorTable();
                for (Error error : errorTable.getErrors()) {
                    writer.write(error.toString() + "\n");
                }
                if (errorTable.getErrors().size() > 0 ) {
                    hasError = true;
                }
            } catch (IOException e) {
                System.out.println("output.txt 写入失败");
            }
        }
        if (!hasError) {
            LlvmIr llvmIr = new LlvmIr(compUnitNode);
            LlvmIrModule llvmIrModule = llvmIr.generateIrModule();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mid_output_origin))) {
                    ArrayList<String> ans = llvmIrModule.midOutput();
                    for (String s : ans) {
                        writer.write(s);
                    }
                } catch (IOException e) {
                    System.out.println("output.txt 写入失败");
                }
            if (isCompetition) {
                Opt opt = new Opt(llvmIrModule,llvmIr.getBasicBlockCnt());
                opt.doOpt();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mid_output))) {
                    ArrayList<String> ans = llvmIrModule.midOutput();
                    for (String s : ans) {
                        writer.write(s);
                    }
                } catch (IOException e) {
                    System.out.println("output.txt 写入失败");
                }
            }
            Mips mips = new Mips(llvmIrModule);
            MipsModule mipsModule = mips.generateMips();
            if (testMips) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mips_output))) {
                    ArrayList<String> mipsAns = mipsModule.mipsOutput();
                    for (String s : mipsAns) {
                        writer.write(s);
                    }
                } catch (IOException e) {
                    System.out.println("output.txt 写入失败");
                }
            }
        }
    }
}
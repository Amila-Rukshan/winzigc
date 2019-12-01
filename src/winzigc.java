
/*
 *
 *          compile : javac winzigc.java
 *              run : java winzigc -ast winzig_test_programs/winzig_01
 *  compile and run : javac winzigc.java; java winzigc -ast winzig_test_programs/winzig_01
 *         test all : javac winzigc.java; java winzigc -test
 *       vm options : -Xss40m (more memory for jvm)
 *
 */

/*
 *  INFO: Two parsers were written depending on tree generation method (bottom up or to down way)
 *        Parser for top down parse and bottom up generation is enabled in main method
 *        Left child right sibling binary tree is the AST tree representation
 *        No nested comments are handled
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class winzigc {

    private static List<SyntaxToken> tokenStream = new ArrayList<>();

    public static void main(String[] args) {
        String flag = args[0];
        switch (flag){
            case  "-ast":
                String path_to_winzig_program = args[1];


                String program_as_a_string = null;
                try {
                    program_as_a_string = readWinzigProgram(path_to_winzig_program);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Lexer lexer = new Lexer(program_as_a_string);

                SyntaxKind kind;
                do{
                    SyntaxToken token = lexer.findNextToken();
                    tokenStream.add(token);
                    kind = token.kind;
                }
                while(kind != SyntaxKind.EndOfProgramToken);  //  & kind != SyntaxKind.BadToken

                List<SyntaxToken> screenedTokenStream = screenTokenStream();

//              Enable top down or bottom up tree generating parser

//              ParserTopDownTree parser = new ParserTopDownTree(screenedTokenStream);
                ParserBottomUpTree parser = new ParserBottomUpTree(screenedTokenStream);
                break;
            case "-test":
                for(int i = 1; i <= 15; i++){
                    String path =  String.format("winzig_test_programs/winzig_%02d" , i);
                    System.out.println("================================= "+ path+" ===========================================");
                    // call lexer and parser

                    String program_string = null;
                    try {
                        program_string = readWinzigProgram(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Lexer lex = new Lexer(program_string);

                    SyntaxKind k;
                    do{
                        SyntaxToken token = lex.findNextToken();
                        tokenStream.add(token);
                        k = token.kind;
                    }
                    while(k != SyntaxKind.EndOfProgramToken);  //  & kind != SyntaxKind.BadToken


//                    ParserTopDownTree p = new ParserTopDownTree(screenTokenStream());
                    ParserBottomUpTree p = new ParserBottomUpTree(screenTokenStream());
                    tokenStream = new ArrayList<>();
                }
                break;
            case  "-h":
                System.out.println("run command: java winzigc [stage] [path]");
                System.out.println("    [stage]: specifies where to stop in the steps of winzigc compiler");
                System.out.println("            -ast: generate 'Abstract Syntax Tree' from the winzig program found using file path given by [path]");
                System.out.println("     [path]: relative path to the winzig program");
                break;
            default:
                System.out.println("Provided args are incompatible. Run with flag -h for help");
                System.out.println("java winzigc -h");
        }
    }

    private static String readWinzigProgram(String path) throws IOException {
//        System.out.println(new File("").getAbsolutePath()+"/"+path);
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String         line;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            return stringBuilder.toString()+"   ";
        } finally {
            reader.close();
        }
    }

    private static List<SyntaxToken> screenTokenStream(){
        List<SyntaxToken> screenedToken = new ArrayList<>();
        for(SyntaxToken token : tokenStream){
            if(token.kind != SyntaxKind.CommentToken & token.kind != SyntaxKind.WhiteSpaceToken & token.kind != SyntaxKind.NewlineToken & token.kind != SyntaxKind.EndOfProgramToken){
                screenedToken.add(token);
            }
        }
        return screenedToken;
    }
}












/*
 *
 *          compile : javac winzigc.java
 *              run : java Program
 *  compile and run : javac winzigc.java; java winzigc –ast winzig_test_programs/winzig_01
 *
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class winzigc {

    private static List<SyntaxToken> tokenStream = new ArrayList<SyntaxToken>();

    public static void main(String[] args) {
        String flag = args[0];
        switch (flag){
            case  "-ast":
                String path_to_winzig_program = args[1];
                System.out.println("generate AST for "+ path_to_winzig_program);
                // call lexer and parser

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
                while(kind != SyntaxKind.EndOfProgramToken & kind != SyntaxKind.BadToken);

                for(SyntaxToken token: tokenStream){
                    System.out.println(token.kind+" : "+token.position+" >"+token.text+"<");
                }

                break;
            case "-codegen":
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

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
}

enum SyntaxKind {
    // for the lexer
    IdentifierToken,
    IntegerToken,
    WhiteSpaceToken,
    CharToken,
    StringToken,
    CommentToken,
    NewlineToken,               //  \n
    ProgramToken,               //  program
    VarToken,                   //  var
    ConstToken,                 //  const
    TypeToken,                  //  type
    FunctionToken,              //  function
    ReturnToken,                //  return
    BeginToken,                 //  begin
    EndToken,                   //  end
    SwapToken,                  //  :=:
    AssignToken,                //  :=
    OutputToken,                //  output
    IfToken,                    //  if
    ThenToken,                  //  then
    ElseToken,                  //  else
    WhileToken,                 //  while
    DoToken,                    //  do
    CaseToken,                  //  case
    OfToken,                    //  of
    CaseExpToken,               //  ..
    OtherwiseToken,             //  otherwise
    RepeatToken,                //  repeat
    ForToken,                   //  for
    UntilToken,                 //  until
    LoopToken,                  //  loop
    PoolToken,                  //  pool
    ExitToken,                  //  exit
    LessOrEqualOprToken,        //  <=
    NotEqualOprToken,           //  <>
    LessThanOprToken,           //  <
    GreaterOrEqualOprToken,     //  >=
    GreaterThanOprToken,        //  >
    EqualToOprToken,            //  =
    ModulusOprToken,            //  mod
    AndOprToken,                //  and
    OrOprToken,                 //  or
    NotOprToken,                //  not
    ReadToken,                  //  read
    SuccessorToken,             //  succ
    PredecessorToken,           //  pred
    CharFuncToken,              //  chr
    OrdinalFuncToken,           //  ord
    EndOfFileToken,             //  eof
    ColonToken,                 //  :
    SemiColonToken,             //  ;
    SingleDotToken,             //  .
    CommaToken,                 //  ,
    OpenBracketToken,           //  (
    CloseBracketToken,          //  )
    PlusToken,                  //  +
    MinusToken,                 //  -
    MultiplyToken,              //  *
    DivideToken,                //  /
    EndOfProgramToken,
    BadToken


//    private final Pattern pattern;
//
//    SyntaxKind(String regex) {
//        pattern = Pattern.compile("^" + regex);
//    }
//
//    int endOfMatch(String s) {
//        Matcher m = pattern.matcher(s);
//
//        if (m.find()) {
//            return m.end();
//        }
//        return -1;
//    }
}

class SyntaxToken {
    SyntaxKind kind;
    int position;
    String text;

    public SyntaxToken(SyntaxKind kind, int position, String text){
        this.kind = kind;
        this.position = position;
        this.text = text;
    }
}

class Lexer{

    private final String _text;

    private int _position;

    private char getCurrentChar(){
        if(_position >= _text.length())
            return '\0';
        return _text.charAt(_position);
    }

    public Lexer(String text){
        _text = text;
    }

    private void Next(){
        _position += 1;
    }

    public SyntaxToken findNextToken(){
        if(_position >= _text.length()){
            return new SyntaxToken(SyntaxKind.EndOfProgramToken, _position, "\0");
        }

        Pattern pattern;
        String remaining_text;
        Matcher m;

        // find identifiers and alphabetic syntax token
        pattern = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*");
        remaining_text = _text.substring(_position, _text.length());
        m = pattern.matcher(remaining_text);

        if(m.find()){
            int start = _position;
            int end = m.end();
            _position += end;

            String lexeme = remaining_text.substring(0, end);
            switch(lexeme){
                case "program": return new SyntaxToken(SyntaxKind.ProgramToken, start, lexeme);
                case "var": return new SyntaxToken(SyntaxKind.VarToken, start, lexeme);
                case "const": return new SyntaxToken(SyntaxKind.ConstToken, start, lexeme);
                case "type": return new SyntaxToken(SyntaxKind.TypeToken, start, lexeme);
                case "function": return new SyntaxToken(SyntaxKind.FunctionToken, start, lexeme);
                case "return": return new SyntaxToken(SyntaxKind.ReturnToken, start, lexeme);
                case "begin": return new SyntaxToken(SyntaxKind.BeginToken, start, lexeme);
                case "end": return new SyntaxToken(SyntaxKind.EndToken, start, lexeme);
                case "output": return new SyntaxToken(SyntaxKind.OutputToken, start, lexeme);
                case "if": return new SyntaxToken(SyntaxKind.IfToken, start, lexeme);
                case "then": return new SyntaxToken(SyntaxKind.ThenToken, start, lexeme);
                case "else": return new SyntaxToken(SyntaxKind.ElseToken, start, lexeme);
                case "while": return new SyntaxToken(SyntaxKind.WhileToken, start, lexeme);
                case "do": return new SyntaxToken(SyntaxKind.DoToken, start, lexeme);
                case "case": return new SyntaxToken(SyntaxKind.CaseToken, start, lexeme);
                case "of": return new SyntaxToken(SyntaxKind.OfToken, start, lexeme);
                case "otherwise": return new SyntaxToken(SyntaxKind.OtherwiseToken, start, lexeme);
                case "repeat": return new SyntaxToken(SyntaxKind.RepeatToken, start, lexeme);
                case "for": return new SyntaxToken(SyntaxKind.ForToken, start, lexeme);
                case "until": return new SyntaxToken(SyntaxKind.UntilToken, start, lexeme);
                case "loop": return new SyntaxToken(SyntaxKind.LoopToken, start, lexeme);
                case "pool": return new SyntaxToken(SyntaxKind.PoolToken, start, lexeme);
                case "exit": return new SyntaxToken(SyntaxKind.ExitToken, start, lexeme);
                case "mod": return new SyntaxToken(SyntaxKind.ModulusOprToken, start, lexeme);
                case "or": return new SyntaxToken(SyntaxKind.OrOprToken, start, lexeme);
                case "and": return new SyntaxToken(SyntaxKind.AndOprToken, start, lexeme);
                case "not": return new SyntaxToken(SyntaxKind.NotOprToken, start, lexeme);
                case "read": return new SyntaxToken(SyntaxKind.ReadToken, start, lexeme);
                case "succ": return new SyntaxToken(SyntaxKind.SuccessorToken, start, lexeme);
                case "pred": return new SyntaxToken(SyntaxKind.PredecessorToken, start, lexeme);
                case "chr": return new SyntaxToken(SyntaxKind.CharFuncToken, start, lexeme);
                case "ord": return new SyntaxToken(SyntaxKind.OrdinalFuncToken, start, lexeme);
                case "eof": return new SyntaxToken(SyntaxKind.EndOfFileToken, start, lexeme);
                default: return new SyntaxToken(SyntaxKind.IdentifierToken, start, lexeme);
            }
        }

        // white spaces
        pattern = Pattern.compile("^(\\s|\\v|\\h|\\n)+");
        remaining_text = _text.substring(_position, _text.length());
        m = pattern.matcher(remaining_text);

        if(m.find()){
            int start = _position;
            int end = m.end();
            _position += end;

            String lexeme = remaining_text.substring(0, end);
            return new SyntaxToken(SyntaxKind.WhiteSpaceToken, start, lexeme);
        }

        // integers
        pattern = Pattern.compile("^[\\d]+");
        remaining_text = _text.substring(_position, _text.length());
        m = pattern.matcher(remaining_text);

        if(m.find()){
            int start = _position;
            int end = m.end();
            _position += end;

            String lexeme = remaining_text.substring(0, end);
            return new SyntaxToken(SyntaxKind.IntegerToken, start, lexeme);
        }

        return new SyntaxToken(SyntaxKind.BadToken, _position, null);
    }

}






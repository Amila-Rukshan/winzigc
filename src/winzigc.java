
/*
 *       vm options : -Xss40m
 *          compile : javac winzigc.java
 *              run : java Program
 *  compile and run : javac winzigc.java; java winzigc -ast winzig_test_programs/winzig_01
 *         test all : javac winzigc.java; java winzigc -test
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
                while(kind != SyntaxKind.EndOfProgramToken);  //  & kind != SyntaxKind.BadToken

                for(SyntaxToken token: screenTokenStream()){
                    System.out.format("%-30s%5s%20s%15s", token.kind, token.position, token.type, token.text+"\n");
                }

                break;
            case "-test":
                for(int i = 1; i <= 25; i++){
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

                    for(SyntaxToken token: screenTokenStream()){
                        System.out.format("%-30s%5s%20s%15s", token.kind, token.position, token.type, token.text+"\n");
                    }
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
        System.out.println(new File("").getAbsolutePath()+"/"+path);
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

}

class SyntaxToken {
    SyntaxKind kind;
    int position;
    String text;
    String type;

    public SyntaxToken(SyntaxKind kind, int position, String text, String type){
        this.kind = kind;
        this.position = position;
        this.text = text;
        this.type = type;
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

    String findIdentifiersAndSyntax(){
        String identiferChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        int start = _position;
        if(identiferChars.indexOf(getCurrentChar()) >= 9){
            _position++;
            while(identiferChars.indexOf(getCurrentChar()) >= 0){
                _position++;
            }
            return _text.substring(start, _position);
        }
        return null;
    }

    SyntaxToken findCommentTypeOne(){
        int start = _position;
        if(getCurrentChar() == '{'){
            _position++;
            while(getCurrentChar() != '}'){
                _position++;
            }
            _position++;
            return new SyntaxToken(SyntaxKind.CommentToken, start, _text.substring(start, _position), "#COMMENT");
        }
        return null;
    }

    SyntaxToken findCommentTypeTwo(){
        int start = _position;
        if(getCurrentChar() == '#'){
            _position++;
            while(getCurrentChar() != '\n'){
                _position++;
            }
            return new SyntaxToken(SyntaxKind.CommentToken, start, _text.substring(start, _position), "#COMMENT");
        }
        return null;
    }

    SyntaxToken findNewLine(){
        int start = _position;
        if(getCurrentChar() == '\n'){
            _position++;
            return new SyntaxToken(SyntaxKind.NewlineToken, start, "\n", "NEWLINE");
        }
        return null;
    }

    SyntaxToken findWhiteSpace(){
        char[] spaceChars = { ' ', '\f', '\r', '\t' };
        int start = _position;
        if (charIsInArray(getCurrentChar(), spaceChars)) {
            _position++;
            while (charIsInArray(getCurrentChar(), spaceChars)){
                _position++;
            }
            return new SyntaxToken(SyntaxKind.WhiteSpaceToken, start, _text.substring(start, _position), "WHITESPACE");
        }
        return null;
    }

    SyntaxToken findInteger(){
        char[] intChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        int start  = _position;
        if (charIsInArray(getCurrentChar(), intChars)) {
            _position++;
            while (charIsInArray(getCurrentChar(), intChars)){
                _position++;
            }
            return new SyntaxToken(SyntaxKind.IntegerToken, start, _text.substring(start, _position), "<integer>");
        }
        return null;
    }

    SyntaxToken findChars(){
        int start = _position;
        if(getCurrentChar() == '\'' & _text.charAt(_position+2 ) == '\'' & _text.charAt(_position+1 ) != '\''){
            _position += 3;
            return new SyntaxToken(SyntaxKind.CharToken, start, _text.substring(start, _position), "<char>");
        }
        return  null;
    }

    SyntaxToken findStrings(){
        int start = _position;
        if(getCurrentChar() == '"'){
            _position++;
            while(getCurrentChar() != '"'){
                _position++;
            }
            _position++;
            return new SyntaxToken(SyntaxKind.StringToken, start, _text.substring(start, _position), "<string>");
        }
        return null;
    }

    boolean charIsInArray(char c, char[] charArray){
        for(char spaceChar : charArray){
            if(spaceChar == c){
                return true;
            }
        }
        return false;
    }

    public SyntaxToken findNextToken(){

        // end of the program token
        if(_position >= _text.length()){ return new SyntaxToken(SyntaxKind.EndOfProgramToken, _position, "\0", null);}

        // find identifiers and alphabetic syntax token
        String lexeme = findIdentifiersAndSyntax();
        if(lexeme != null){
            int start = _position;
            switch(lexeme){
                case "program": return new SyntaxToken(SyntaxKind.ProgramToken, start, lexeme, "program");
                case "var": return new SyntaxToken(SyntaxKind.VarToken, start, lexeme, "var");
                case "const": return new SyntaxToken(SyntaxKind.ConstToken, start, lexeme, "const");
                case "type": return new SyntaxToken(SyntaxKind.TypeToken, start, lexeme,"type");
                case "function": return new SyntaxToken(SyntaxKind.FunctionToken, start, lexeme,"function");
                case "return": return new SyntaxToken(SyntaxKind.ReturnToken, start, lexeme,"return");
                case "begin": return new SyntaxToken(SyntaxKind.BeginToken, start, lexeme, "begin");
                case "end": return new SyntaxToken(SyntaxKind.EndToken, start, lexeme, "end");
                case "output": return new SyntaxToken(SyntaxKind.OutputToken, start, lexeme,"output");
                case "if": return new SyntaxToken(SyntaxKind.IfToken, start, lexeme, "if");
                case "then": return new SyntaxToken(SyntaxKind.ThenToken, start, lexeme,"then");
                case "else": return new SyntaxToken(SyntaxKind.ElseToken, start, lexeme,"else");
                case "while": return new SyntaxToken(SyntaxKind.WhileToken, start, lexeme,"while");
                case "do": return new SyntaxToken(SyntaxKind.DoToken, start, lexeme,"do");
                case "case": return new SyntaxToken(SyntaxKind.CaseToken, start, lexeme,"case");
                case "of": return new SyntaxToken(SyntaxKind.OfToken, start, lexeme,"of");
                case "otherwise": return new SyntaxToken(SyntaxKind.OtherwiseToken, start, lexeme,"otherwise");
                case "repeat": return new SyntaxToken(SyntaxKind.RepeatToken, start, lexeme,"repeat");
                case "for": return new SyntaxToken(SyntaxKind.ForToken, start, lexeme,"for");
                case "until": return new SyntaxToken(SyntaxKind.UntilToken, start, lexeme,"until");
                case "loop": return new SyntaxToken(SyntaxKind.LoopToken, start, lexeme,"loop");
                case "pool": return new SyntaxToken(SyntaxKind.PoolToken, start, lexeme,"pool");
                case "exit": return new SyntaxToken(SyntaxKind.ExitToken, start, lexeme,"exit");
                case "mod": return new SyntaxToken(SyntaxKind.ModulusOprToken, start, lexeme,"mod");
                case "or": return new SyntaxToken(SyntaxKind.OrOprToken, start, lexeme,"or");
                case "and": return new SyntaxToken(SyntaxKind.AndOprToken, start, lexeme,"and");
                case "not": return new SyntaxToken(SyntaxKind.NotOprToken, start, lexeme,"not");
                case "read": return new SyntaxToken(SyntaxKind.ReadToken, start, lexeme,"read");
                case "succ": return new SyntaxToken(SyntaxKind.SuccessorToken, start, lexeme,"succ");
                case "pred": return new SyntaxToken(SyntaxKind.PredecessorToken, start, lexeme,"pred");
                case "chr": return new SyntaxToken(SyntaxKind.CharFuncToken, start, lexeme,"chr");
                case "ord": return new SyntaxToken(SyntaxKind.OrdinalFuncToken, start, lexeme,"ord");
                case "eof": return new SyntaxToken(SyntaxKind.EndOfFileToken, start, lexeme,"eof");
                default: return new SyntaxToken(SyntaxKind.IdentifierToken, start, lexeme, "<identifier>");
            }
        }

        // comment type 1
        SyntaxToken commentTypeOne = findCommentTypeOne();
        if(commentTypeOne != null){ return commentTypeOne; }

        // comment type 2
        SyntaxToken commentTypeTwo = findCommentTypeTwo();
        if(commentTypeTwo != null){ return commentTypeTwo; }

        // new line
        SyntaxToken newLine = findNewLine();
        if(newLine != null){ return newLine; }

        // white spaces
        SyntaxToken whiteSpace = findWhiteSpace();
        if(whiteSpace != null){ return whiteSpace; }

        // integers
        SyntaxToken integers = findInteger();
        if(integers != null){ return integers; }

        // chars
        SyntaxToken chars = findChars();
        if(chars != null){ return chars; }

        // strings
        SyntaxToken strings = findStrings();
        if(strings != null){ return strings; }

        // swap :=:
        String syntax_len_3 = _text.substring(_position, _position+3);
        if(syntax_len_3.equals(":=:")){
            return new SyntaxToken(SyntaxKind.SwapToken, _position+=3, syntax_len_3, ":=:");
        }

        // length 2 syntax tokens
        // ":=", "..", "<=", "<>", ">="
        String syntax_len_2 = _text.substring(_position, _position+2);
        switch(syntax_len_2){
            case ":=" : return new SyntaxToken(SyntaxKind.AssignToken, _position+=2, syntax_len_2, ":=");
            case ".." : return new SyntaxToken(SyntaxKind.CaseExpToken, _position+=2, syntax_len_2, "..");
            case "<=" : return new SyntaxToken(SyntaxKind.LessOrEqualOprToken, _position+=2, syntax_len_2, "<=");
            case "<>" : return new SyntaxToken(SyntaxKind.NotEqualOprToken, _position+=2, syntax_len_2,"<>");
            case ">=" : return new SyntaxToken(SyntaxKind.GreaterOrEqualOprToken, _position+=2, syntax_len_2,">=");
        }

        Character next =  getCurrentChar();
        //  ":", ".", "<", ">", "=", ";", ",", "(", ")", "+", "-", "*", "/"
        switch(next){
            case ':': return new SyntaxToken(SyntaxKind.ColonToken, ++_position, next.toString(),":");
            case '.' : return new SyntaxToken(SyntaxKind.SingleDotToken, ++_position, next.toString(),".");
            case '<' : return new SyntaxToken(SyntaxKind.LessThanOprToken, ++_position, next.toString(),"<");
            case '>' : return new SyntaxToken(SyntaxKind.GreaterThanOprToken, ++_position, next.toString(),">");
            case '=' : return new SyntaxToken(SyntaxKind.EqualToOprToken, ++_position, next.toString(),"=");
            case ';' : return new SyntaxToken(SyntaxKind.SemiColonToken, ++_position, next.toString(),";");
            case ',' : return new SyntaxToken(SyntaxKind.CommaToken, ++_position, next.toString(),",");
            case '(' : return new SyntaxToken(SyntaxKind.OpenBracketToken, ++_position, next.toString(),"(");
            case ')' : return new SyntaxToken(SyntaxKind.CloseBracketToken, ++_position, next.toString(),")");
            case '+' : return new SyntaxToken(SyntaxKind.PlusToken, ++_position, next.toString(),"+");
            case '-' : return new SyntaxToken(SyntaxKind.MinusToken, ++_position, next.toString(),"-");
            case '*' : return new SyntaxToken(SyntaxKind.MultiplyToken, ++_position, next.toString(),"*");
            case '/' : return new SyntaxToken(SyntaxKind.DivideToken, ++_position, next.toString(),"/");
        }

        return new SyntaxToken(SyntaxKind.BadToken, _position, null,"UNKNOWN_TOKEN");
    }
}

// to represent nodes in ABS
class ASTNode{

    private String node_label;
    private List<ASTNode> childNodes;

    private ASTNode parent;

    ASTNode(String node_label, SyntaxToken syntaxToken){
        this.childNodes = new ArrayList<>();
    }

    public List<ASTNode> getChildNodes() {
        return childNodes;
    }

    public void setParentNode(ASTNode parent){
        this.parent = parent;
    }

    public void addChildNode(ASTNode child){
        this.childNodes.add(child);
        child.setParentNode(this);
    }

    public ASTNode getParent(){
        return parent;
    }

    public boolean isLeafNode(){
        return this.childNodes.size() == 0;
    }

    // Do a Depth First Traverse to print all the nodes
    public void DFTraverse(int depth){
        for(int i = 0 ; i < depth; i++ ) System.out.print(". ");
        System.out.println("("+ this.childNodes.size() +")");
        if(!isLeafNode()){
            for(ASTNode node: childNodes){
                node.DFTraverse(depth+1);
            }
        }
    }

}

// this is to hold the tree root
class ASTree{
    private ASTNode rootNode;

    ASTree(ASTNode root){
        rootNode = root;
    }

    public ASTNode getRootNode(){
        return rootNode;
    }

    public void TraverseFromTheRoot(){
        rootNode.DFTraverse(0);
    }
}

class Parser{

    private List<SyntaxToken> tokenStream;

    private int tokenIndex;

    SyntaxToken nextToken;

    Parser(List<SyntaxToken> tokenStream){
        this.tokenStream = tokenStream;
    }

    // set the next token and increment the index
    public void getNextToken(){
        nextToken = tokenStream.get(tokenIndex);
        tokenIndex++;
    }

    // look what's the next token without incrementing the token index
    public SyntaxToken peekToken(){
        return tokenStream.get(tokenIndex);
    }





}






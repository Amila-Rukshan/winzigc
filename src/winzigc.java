
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
import java.util.Stack;

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

//                ParserTopDownTree parser = new ParserTopDownTree(screenedTokenStream);
                ParserBottomUpTree parser = new ParserBottomUpTree(screenedTokenStream);
                break;
            case "-test":
                for(int i = 14; i <= 25; i++){
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
        if(identiferChars.indexOf(getCurrentChar()) >= 10){
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

// to represent nodes in AST
class ASTNode{

    private String node_label;
    private List<ASTNode> childNodes;

    private ASTNode parent;

    ASTNode(String node_label){
        this.childNodes = new ArrayList<>();
        this.node_label = node_label;
    }

    public List<ASTNode> getChildNodes() {
        return childNodes;
    }

    int getChildNodesCount() {
        return childNodes.size();
    }

    public void setParentNode(ASTNode parent){
        this.parent = parent;
    }

    public void addChildNode(ASTNode child){
        this.childNodes.add(child);
        child.setParentNode(this);
    }

    ASTNode deleteASTNode(int index){
        return childNodes.remove(index);
    }

    void addChildAtIndex(int index, ASTNode child){
        childNodes.add(index, child);
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
        System.out.print(this.node_label);
        System.out.println("("+ this.childNodes.size() +")");
        if(!isLeafNode()){
            for(ASTNode node: childNodes){
                node.DFTraverse(depth+1);
            }
        }
    }

    public String getNode_label() {
        return node_label;
    }
}

abstract class Parser{
    // Base class for the Parsers
}

class ParserTopDownTree extends Parser{

    private ASTNode rootNode;

    private List<SyntaxToken> tokenStream;

    private int tokenIndex;

    SyntaxToken nextToken;

    ParserTopDownTree(List<SyntaxToken> tokenStream){
        this.tokenStream = tokenStream;
        getNextToken();
        winZigAST();
    }

    boolean hasNext(){
        return tokenIndex <= tokenStream.size()-1;
    }

    String peek(){
        if(tokenIndex <= tokenStream.size()-1){
            return tokenStream.get(tokenIndex).type;
        }
        System.out.println("TOKEN ARE OVER");
        throw new Error();
    }

    // set the next token and increment the index
    void getNextToken(){
        nextToken = tokenStream.get(tokenIndex);
        tokenIndex++;
    }

    // look what's the next token without incrementing the token index
    public SyntaxToken peekToken(){
        return tokenStream.get(tokenIndex);
    }

    void winZigAST(){
        rootNode = new ASTNode("program");

        read("program");
        Name(rootNode);
        read(":");
        Consts(rootNode);
        Types(rootNode);
        Dclns(rootNode);
        SubProgs(rootNode);
        Body(rootNode);
        Name(rootNode);
        read(".");

//        System.out.println(nextToken.type);

        rootNode.DFTraverse(0);
    }

    void Name(ASTNode parent){
        read(SyntaxKind.IdentifierToken, parent);
    }

    void Consts(ASTNode parent){
        if(nextToken.type == "const"){
            ASTNode constsNode = addASTNode(parent, "consts");
            read("const");
            int list_count = 1;
            Const(constsNode);
            while(nextToken.type != ";"){
                read(",");
                Const(constsNode);
            }
            read(";");
        }else{
            addASTNode(parent, "consts");
        }
    }

    void Const(ASTNode parent){
        Name(parent);
        read("=");
        ConstValue(parent);
    }

    void ConstValue(ASTNode parent){
        // skip <char> or <integer> but create a node for <identifier>
        if(nextToken.type.equals("<char>") || nextToken.type.equals("<integer>") ){
            read(nextToken.kind ,parent);
        }

        if(nextToken.type == "<identifier>"){
            Name(parent);
        }
    }

    void Types(ASTNode parent){
        if(nextToken.type == "type"){
            ASTNode typesNode = addASTNode(parent, "types");
            read("type");

            while(nextToken.type == "<identifier>"){
                Type(typesNode);
                read(";");
            }
        }else{
            ASTNode typesNode = addASTNode(parent, "types");
        }
    }

    void Type(ASTNode parent){
        ASTNode typeNode = new ASTNode("type");
        parent.addChildNode(typeNode);

        if(nextToken.type == "<identifier>"){
            read(SyntaxKind.IdentifierToken, typeNode);
            read("=");
            LitList(typeNode);
        }
    }

    void LitList(ASTNode parent){
        ASTNode litNode = new ASTNode("lit");
        parent.addChildNode(litNode);

        read("(");
        Name(litNode);
        while(nextToken.type != ")"){
            read(",");
            Name(litNode);
        }
        read(")");
    }

    void Dclns(ASTNode parent){
        if(nextToken.type == "var"){
            ASTNode dclnsNode = addASTNode(parent, "dclns");
            read("var");
            Dcln(dclnsNode);
            read(";");
            while(nextToken.type == "<identifier>"){
                Dcln(dclnsNode);
                read(";");
            }
        }else{
            addASTNode(parent, "dclns");
        }
    }

    void Dcln(ASTNode parent){
        ASTNode varNode = addASTNode(parent, "var");
        Name(varNode);
        while(nextToken.type != ":"){
            read(",");
            Name(varNode);
        }
        read(":");
        Name(varNode);
    }

    void SubProgs(ASTNode parent){
        ASTNode subprogsNode = addASTNode(parent, "subprogs");
        while(nextToken.kind == SyntaxKind.FunctionToken){
            Fcn(subprogsNode);
        }
    }

    void Fcn(ASTNode parent){
        ASTNode fcnNode = addASTNode(parent, "fcn");
        read("function");
        Name(fcnNode);
        read("(");
        Params(fcnNode);
        read(")");
        read(":");
        Name(fcnNode);
        read(";");
        Consts(fcnNode);
        Types(fcnNode);
        Dclns(fcnNode);
        Body(fcnNode);
        Name(fcnNode);
        read(";");
    }

    void Params(ASTNode parent){
        ASTNode paramsNode = addASTNode(parent, "params");
        Dcln(paramsNode);
        while(nextToken.type == ";"){
            read(";");
            Dcln(paramsNode);
        }
    }

    void Body(ASTNode parent){
        ASTNode blockNode = addASTNode(parent, "block");
        read("begin");
        Statement(blockNode);
        while(nextToken.type == ";"){
            read(";");
            Statement(blockNode);
        }
        read("end");

    }

    void Statement(ASTNode parent){

        switch(nextToken.type){
            case "if":
                ASTNode ifNode = addASTNode(parent, "if");
                read("if");
                Expression(ifNode);
                read("then");
                Statement(ifNode);
                if(nextToken.type == "else"){
                    read("else");
                    Statement(ifNode);
                }
                break;
            case "for":
                ASTNode forNode = addASTNode(parent, "for");
                read("for");
                read("(");
                ForStat(forNode);
                read(";");
                ForExp(forNode);
                read(";");
                ForStat(forNode);
                read(")");
                Statement(forNode);
                break;
            case "while":
                ASTNode whileNode = addASTNode(parent, "while");
                read("while");
                Expression(whileNode);
                read("do");
                Statement(whileNode);
                break;
            case "repeat":
                ASTNode repeatNode = addASTNode(parent, "repeat");
                read("repeat");
                Statement(repeatNode);
                while(nextToken.type == ";"){
                    read(";");
                    Statement(repeatNode);
                }
                read("until");
                Expression(repeatNode);
                break;
            case "loop":
                ASTNode loopNode = addASTNode(parent, "loop");
                read("loop");
                Statement(loopNode);
                while(nextToken.type == ";"){
                    read(";");
                    Statement(loopNode);
                }
                read("pool");
                break;
            case "output":
                ASTNode outputNode = addASTNode(parent, "output");
                read("output");
                read("(");
                OutEXp(outputNode);
                // out exp list
                while(nextToken.type == ","){
                    read(",");
                    OutEXp(outputNode);
                }
                read(")");
                break;
            case "exit":
                ASTNode exitNode = addASTNode(parent, "exit");
                read("exit");
                break;
            case "return":
                ASTNode returnNode = addASTNode(parent, "return");
                read("return");
                Expression(returnNode);
                break;
            case "read":
                ASTNode readNode = addASTNode(parent, "read");
                read("read");
                read("(");
                Name(readNode);
                while(nextToken.type == ","){
                    read(",");
                    Name(readNode);
                }
                read(")");
                break;
            case "case":
                ASTNode caseNode = addASTNode(parent, "case");
                read("case");
                Expression(caseNode);
                read("of");
                Caseclauses(caseNode);
                OtherwiseClause(caseNode);
                read("end");
                break;
            case "<identifier>":
                Assignment(parent);
                break;
            case "begin":
                Body(parent);
                break;
            default:
                addASTNode(parent, "<null>");
                break;
        }
    }

    void Caseclauses(ASTNode parent){
        Caseclause(parent);
        read(";");
        while(nextToken.type == "<integer>" || nextToken.type == "<char>" || nextToken.type == "<identifier>"){
            Caseclause(parent);
            read(";");
        }
    }

    void Caseclause(ASTNode parent){
        ASTNode case_clauseNode = addASTNode(parent, "case_clause");
        CaseExpression(case_clauseNode);
        while(nextToken.type == ","){
            read(",");
            CaseExpression(case_clauseNode);
        }
        read(":");
        Statement(case_clauseNode);
    }

    void CaseExpression(ASTNode parent){
        ConstValue(parent);
        if(nextToken.type == ".."){
            ASTNode doubleDot = addASTNode(parent, "..");
            read("..");
            doubleDot.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
            ConstValue(doubleDot);
        }
    }

    void OtherwiseClause(ASTNode parent){
        if(nextToken.type == "otherwise"){
            ASTNode otherwiseNode = addASTNode(parent, "otherwise");
            read("otherwise");
            Statement(otherwiseNode);
        }else{

        }
    }

    void OutEXp(ASTNode parent){
        if(nextToken.type == "<string>"){
            StringNode(parent);
        }else{
            ASTNode integerNode = addASTNode(parent, "integer");
            Expression(integerNode);
        }
    }

    void StringNode(ASTNode parent){
        read(SyntaxKind.StringToken, parent);
    }

    void ForStat(ASTNode parent){
        if(nextToken.type == ";"){
            ASTNode nullNode = addASTNode(parent, "<null>");
        }else{
            Assignment(parent);
        }
    }

    void ForExp(ASTNode parent){
        if(nextToken.type == ";"){
            addASTNode(parent, "true");
        }else{
            Expression(parent);
        }
    }

    void Assignment(ASTNode parent){

        switch(peek()){
            case ":=":
                ASTNode assignNode = addASTNode(parent, "assign");
                Name(assignNode);
                read(":=");
                Expression(assignNode);
                break;
            case ":=:":
                ASTNode swapNode = addASTNode(parent, "swap");
                Name(swapNode);
                read(":=:");
                Name(swapNode);
                break;
            default:
                System.out.println("ERROR PEEK: "+peek());
                System.out.println("ERROR NEXT: "+nextToken.type);
//                rootNode.DFTraverse(0);
                throw new Error();
        }

    }

    void Expression(ASTNode parent){
        Term(parent);
        if(nextToken.type == "<=" || nextToken.type == "<" || nextToken.type == ">="|| nextToken.type == ">"|| nextToken.type == "="|| nextToken.type == "<>"){
            switch(nextToken.type){
                case "<=":
                    read("<=");
                    ASTNode lessOrEqNode = addASTNode(parent, "<=");
                    lessOrEqNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(lessOrEqNode);
                    break;
                case "<":
                    read("<");
                    ASTNode lessNode = addASTNode(parent, "<");
                    lessNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(lessNode);
                    break;
                case ">=":
                    read(">=");
                    ASTNode greaterOrEqNode = addASTNode(parent, ">=");
                    greaterOrEqNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(greaterOrEqNode);
                    break;
                case ">":
                    read(">");
                    ASTNode greaterNode = addASTNode(parent, ">");
                    greaterNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(greaterNode);
                    break;
                case "=":
                    read("=");
                    ASTNode equalNode = addASTNode(parent, "=");
                    equalNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(equalNode);
                    break;
                case "<>":
                    read("<>");
                    ASTNode InequalNode = addASTNode(parent, "<>");
                    InequalNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(InequalNode);
                    break;
                default:
                    System.out.println("ERROR in Expression");
                    System.out.println("TOKEN WAS: "+nextToken.type);
                    rootNode.DFTraverse(0);
                    throw new Error();
            }
        }
    }

    void Term(ASTNode parent) {
        Factor(parent);
        while (nextToken.type == "+" || nextToken.type == "-" || nextToken.type == "or") {
            switch (nextToken.type) {
                case "+":
                    read("+");
                    ASTNode addNode = addASTNode(parent, "+");
                    addNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(addNode);
                    break;
                case "-":
                    read("-");
                    ASTNode minusNode = addASTNode(parent, "-");
                    minusNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(minusNode);
                    break;
                case "or":
                    read("or");
                    ASTNode orNode = addASTNode(parent, "or");
                    orNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(orNode);
                    break;
                default:
                    System.out.println("ERROR in Term");
                    throw new Error();
            }
        }
    }

    void Factor(ASTNode parent){
        Primary(parent);
        while(nextToken.type == "*" || nextToken.type == "/" ||nextToken.type == "and" ||nextToken.type == "mod" ){
            switch(nextToken.type){
                case "*":
                    read("*");
                    ASTNode mulNode = addASTNode(parent, "*");
                    mulNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(mulNode);
                    break;
                case "/":
                    read("/");
                    ASTNode divNode = addASTNode(parent, "/");
                    divNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(divNode);
                    break;
                case "and":
                    read("and");
                    ASTNode andNode = addASTNode(parent, "and");
                    andNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(andNode);
                    break;
                case "mod":
                    read("mod");
                    ASTNode modNode = addASTNode(parent, "mod");
                    modNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(modNode);
                    break;
            }
        }
    }

    void Primary(ASTNode parent){

        switch(nextToken.type){
            case "<char>":
                read(SyntaxKind.CharToken,parent); break;
            case "<integer>":
//                System.out.println(nextToken.type + " "+nextToken.text);
                read(SyntaxKind.IntegerToken,parent); break;
            case "eof":
                addASTNode(parent, "eof");
                read("eof"); break;
            case "-":
                read("-");
                ASTNode minusNode = addASTNode(parent, "-");
                Primary(minusNode);
                break;
            case "+":
                read("+");
                ASTNode plusNode = addASTNode(parent, "+");
                Primary(plusNode);
                break;
            case "not":
                read("not");
                ASTNode notNode = addASTNode(parent, "not");
                Primary(notNode);
                break;
            case "(":
                read("(");
                Expression(parent);
                read(")");
                break;
            case "succ":
                read("succ");
                read("(");
                ASTNode succNode = addASTNode(parent, "succ");
                Expression(succNode);
                read(")");
                break;
            case "pred":
                read("pred");
                read("(");
                ASTNode predNode = addASTNode(parent, "pred");
                Expression(predNode);
                read(")");
                break;
            case "chr":
                read("chr");
                read("(");
                ASTNode chrNode = addASTNode(parent, "chr");
                Expression(chrNode);
                read(")");
                break;
            case "ord":
                read("ord");
                read("(");
                ASTNode ordNode = addASTNode(parent, "ord");
                Expression(ordNode);
                read(")");
                break;
            case "<identifier>":
                if(peek() == "("){
                    ASTNode callNode = addASTNode(parent, "call");
                    Name(callNode);
                    read("(");
                    Expression(callNode);
                    while(nextToken.type == ","){
                        read(",");
                        Expression(callNode);
                    }
                    read(")");
                }else{
                    Name(parent);
                }
                break;
            default:
                System.out.println("ERROR WHILE PARSING: " + nextToken.type); throw new Error();
        }

    }

    // add the new node to parent node
    ASTNode addASTNode(ASTNode parent, String node_label){
        ASTNode node = new ASTNode(node_label);
        parent.addChildNode(node);
        return node;
    }

    void read(String type){
        if(nextToken.type != type){
            System.out.println("EXPECTED: ->|"+type+"|<-");
            System.out.println("FOUND: "+nextToken.type+" "+nextToken.text);
//            rootNode.DFTraverse(0);
            throw new Error();
        }

        if(hasNext()){
            getNextToken();
        }
    }

    void read(SyntaxKind kind, ASTNode parent){

        if(nextToken.kind != kind){
            System.out.println("EXPECTED: "+kind);
            System.out.println("FOUND: "+nextToken.kind+" "+nextToken.text);
            rootNode.DFTraverse(0);
            throw new Error();
        }

        // SyntaxKind.IdentifierToken
        // SyntaxKind.IntegerToken

        ASTNode node_1 = new ASTNode(nextToken.type);
        parent.addChildNode(node_1);

        ASTNode node_2 = new ASTNode(nextToken.text);
        node_1.addChildNode(node_2);

        getNextToken();

    }
}

class ParserBottomUpTree extends Parser{
    private Stack<ASTNode> treeStack;

    private Stack<BinaryTreeNode> binaryTreeStack;

    private List<SyntaxToken> tokenStream;

    private int tokenIndex;

    SyntaxToken nextToken;

    boolean hasNext(){
        return tokenIndex <= tokenStream.size()-1;
    }

    String peek(){
        if(tokenIndex <= tokenStream.size()-1){
            return tokenStream.get(tokenIndex).type;
        }
        System.out.println("TOKEN ARE OVER");
        throw new Error();
    }

    // set the next token and increment the index
    void getNextToken(){
        nextToken = tokenStream.get(tokenIndex);
        tokenIndex++;
    }

    ParserBottomUpTree(List<SyntaxToken> tokenStream){
        this.tokenStream = tokenStream;
        treeStack = new Stack<>();
        binaryTreeStack = new Stack<>();

        getNextToken();
        winZigAST();
    }

    // look what's the next token without incrementing the token index
    public SyntaxToken peekToken(){
        return tokenStream.get(tokenIndex);
    }

    void winZigAST(){
        read("program");
        Name();
        read(":");
        Consts();
        Types();
        Dclns();
        SubProgs();
        Body();
        Name();
        read(".");

        // change count accordingly
        constructTree("program" ,7);

//        for(ASTNode node : treeStack){
//            node.DFTraverse(0);
//            System.out.println("---------------------");
//        }

        for(BinaryTreeNode node : binaryTreeStack){
            node.PreOrderTraverse(0);
        }

    }

    void Name(){
        read(SyntaxKind.IdentifierToken);
    }

    void Consts(){
        if(nextToken.type == "const"){
            read("const");
            int list_count = 1;
            Const();
            while(nextToken.type != ";"){
                read(",");
                Const();
                list_count += 1;
            }
            read(";");
            constructTree("consts" ,list_count);
        }else{
            constructTree("consts" ,0);
        }
    }

    void Const(){
        Name();
        read("=");
        ConstValue();

        constructTree("const", 2);
    }

    void ConstValue(){
        // skip <char> or <integer> but create a node for <identifier>
        if(nextToken.type.equals("<char>") || nextToken.type.equals("<integer>") ){
            read(nextToken.kind);
        }

        if(nextToken.type == "<identifier>"){
            Name();
        }
    }

    void Types(){
        if(nextToken.type == "type"){
            read("type");
            int count = 0;
            while(nextToken.type == "<identifier>"){
                Type();
                read(";");
                count++;
            }
            constructTree("types", count);

        }else{
            constructTree("types", 0);
        }
    }

    void Type(){
        if(nextToken.type == "<identifier>"){
            Name();
            read("=");
            LitList();

            constructTree("type", 2);
        }
    }

    void LitList(){
        read("(");
        Name();
        int count = 1;
        while(nextToken.type != ")"){
            read(",");
            Name();
            count++;
        }
        read(")");
        constructTree("lit", count);
    }

    void Dclns(){
        if(nextToken.type == "var"){
            read("var");
            Dcln();
            read(";");
            int count = 1;
            while(nextToken.type == "<identifier>"){
                Dcln();
                read(";");
                count++;
            }

            constructTree("dclns", count);
        }else{
            constructTree("dclns", 0);
        }
    }

    void Dcln(){
        Name();
        int count = 1;
        while(nextToken.type != ":"){
            read(",");
            Name();
            count++;
        }
        read(":");
        Name();
        count++;

        constructTree("var", count);
    }

    void SubProgs(){

        Fcn();
        int count = 1;
        while(nextToken.kind == SyntaxKind.FunctionToken){
            Fcn();
            count++;
        }

        constructTree("subprogs", count);
    }

    void Fcn(){
        read("function");
        Name();
        read("(");
        Params();
        read(")");
        read(":");
        Name();
        read(";");
        Consts();
        Types();
        Dclns();
        Body();
        Name();
        read(";");

        constructTree("fcn",8);
    }

    void Params(){
        Dcln();
        int count = 1;
        while(nextToken.type == ";"){
            read(";");
            Dcln();
            count++;
        }

        constructTree("params", count);
    }

    void Body(){
        read("begin");
        Statement();
        int count = 1;
        while(nextToken.type == ";"){
            read(";");
            Statement();
            count++;
        }
        read("end");

        constructTree("block", count);
    }

    void Statement(){
        int count = 0;
        switch(nextToken.type){
            case "if":
                read("if");
                Expression();
                read("then");
                Statement();
                count = 2;
                if(nextToken.type == "else"){
                    read("else");
                    Statement();
                    count++;
                }

                constructTree("if", count);
                break;
            case "for":
                read("for");
                read("(");
                ForStat();
                read(";");
                ForExp();
                read(";");
                ForStat();
                read(")");
                Statement();
                constructTree("for", 4);
                break;
            case "while":
                read("while");
                Expression();
                read("do");
                Statement();
                constructTree("while",2);
                break;
            case "repeat":
                read("repeat");
                Statement();
                count = 1;
                while(nextToken.type == ";"){
                    read(";");
                    Statement();
                    count++;
                }
                read("until");
                Expression();
                count++;
                constructTree("repeat", count);
                break;
            case "loop":
                read("loop");
                Statement();
                count = 1;
                while(nextToken.type == ";"){
                    read(";");
                    Statement();
                    count++;
                }
                read("pool");
                constructTree("loop",count);
                break;
            case "output":
                read("output");
                read("(");
                OutEXp();
                count = 1;
                // out exp list
                while(nextToken.type == ","){
                    read(",");
                    OutEXp();
                    count++;
                }
                read(")");
                constructTree("output",count);
                break;
            case "exit":
                read("exit");
                constructTree("exit", 0);
                break;
            case "return":
                read("return");
                Expression();
                constructTree("return", 1);
                break;
            case "read":
                read("read");
                read("(");
                Name();
                count = 1;
                while(nextToken.type == ","){
                    read(",");
                    Name();
                    count++;
                }
                read(")");
                constructTree("read", count);
                break;
            case "case":
                read("case");
                Expression();
                read("of");
                count = 1;
                count += Caseclauses();
                count += OtherwiseClause();
                read("end");
                constructTree("case", count);
                break;
            case "<identifier>":
                Assignment();
                break;
            case "begin":
                Body();
                break;
            default:
                constructTree("<null>",0);
                break;
        }
    }

    int Caseclauses(){
        Caseclause();
        read(";");
        int count = 1;
        while(nextToken.type == "<integer>" || nextToken.type == "<char>" || nextToken.type == "<identifier>"){
            Caseclause();
            read(";");
            count++;
        }

        return count;
    }

    void Caseclause(){
        CaseExpression();
        int count = 1;
        while(nextToken.type == ","){
            read(",");
            CaseExpression();
            count++;
        }
        read(":");
        Statement();
        count++;
        constructTree("case_clause", count);
    }

    void CaseExpression(){
        ConstValue();
        if(nextToken.type == ".."){
            read("..");
            ConstValue();
            constructTree("..",2);
        }
    }

    int  OtherwiseClause(){
        if(nextToken.type == "otherwise"){
            read("otherwise");
            Statement();
            constructTree("otherwise",1);
            return 1;
        }else{
            return 0;
        }
    }

    void OutEXp(){
        if(nextToken.type == "<string>"){
            StringNode();
        }else{
            Expression();
            constructTree("integer", 1);
        }
    }

    void StringNode(){
        read(SyntaxKind.StringToken);
    }

    void ForStat(){
        if(nextToken.type == ";"){
            constructTree("<null>",0);
        }else{
            Assignment();
        }
    }

    void ForExp(){
        if(nextToken.type == ";"){
            constructTree("true",0);
        }else{
            Expression();
        }
    }

    void Assignment(){
        switch(peek()){
            case ":=":
                Name();
                read(":=");
                Expression();
                constructTree("assign", 2);
                break;
            case ":=:":
                Name();
                read(":=:");
                Name();
                constructTree("swap", 2);
                break;
            default:
                System.out.println("ERROR PEEK: "+peek());
                System.out.println("ERROR NEXT: "+nextToken.type);
                throw new Error();
        }
    }

    void Expression(){
        Term();
        if(nextToken.type == "<=" || nextToken.type == "<" || nextToken.type == ">="|| nextToken.type == ">"|| nextToken.type == "="|| nextToken.type == "<>"){
            switch(nextToken.type){
                case "<=":
                    read("<=");
                    Term();
                    constructTree("<=", 2);
                    break;
                case "<":
                    read("<");
                    Term();
                    constructTree("<", 2);
                    break;
                case ">=":
                    read(">=");
                    Term();
                    constructTree(">=", 2);
                    break;
                case ">":
                    read(">");
                    Term();
                    constructTree(">", 2);
                    break;
                case "=":
                    read("=");
                    Term();
                    constructTree("=", 2);
                    break;
                case "<>":
                    read("<>");
                    Term();
                    constructTree("<>", 2);
                    break;
                default:
                    System.out.println("ERROR in Expression");
                    System.out.println("TOKEN WAS: "+nextToken.type);
                    throw new Error();
            }
        }
    }

    void Term() {
        Factor();
        while (nextToken.type == "+" || nextToken.type == "-" || nextToken.type == "or") {
            switch (nextToken.type) {
                case "+":
                    read("+");
                    Factor();
                    constructTree("+", 2);
                    break;
                case "-":
                    read("-");
                    Factor();
                    constructTree("-", 2);
                    break;
                case "or":
                    read("or");
                    Factor();
                    constructTree("or", 2);
                    break;
                default:
                    System.out.println("ERROR in Term");
                    throw new Error();
            }
        }
    }

    void Factor(){
        Primary();
        while(nextToken.type == "*" || nextToken.type == "/" ||nextToken.type == "and" ||nextToken.type == "mod" ){
            switch(nextToken.type){
                case "*":
                    read("*");
                    Factor();
                    constructTree("*", 2);
                    break;
                case "/":
                    read("/");
                    Factor();
                    constructTree("/", 2);
                    break;
                case "and":
                    read("and");
                    Factor();
                    constructTree("and", 2);
                    break;
                case "mod":
                    read("mod");
                    Factor();
                    constructTree("mod", 2);
                    break;
            }
        }
    }

    void Primary(){

        switch(nextToken.type){
            case "<char>":
                read(SyntaxKind.CharToken); break;
            case "<integer>":
//                System.out.println(nextToken.type + " "+nextToken.text);
                read(SyntaxKind.IntegerToken); break;
            case "eof":
                read("eof");
                constructTree("eof",0);
                break;
            case "-":
                read("-");
                Primary();
                constructTree("-",1);
                break;
            case "+":
                read("+");
                Primary();
                constructTree("+",1);
                break;
            case "not":
                read("not");
                Primary();
                constructTree("not", 1);
                break;
            case "(":
                read("(");
                Expression();
                read(")");
                break;
            case "succ":
                read("succ");
                read("(");
                Expression();
                read(")");
                constructTree("succ", 1);
                break;
            case "pred":
                read("pred");
                read("(");
                Expression();
                read(")");
                constructTree("pred", 1);
                break;
            case "chr":
                read("chr");
                read("(");
                Expression();
                read(")");
                constructTree("chr", 1);
                break;
            case "ord":
                read("ord");
                read("(");
                Expression();
                read(")");
                constructTree("ord", 1);
                break;
            case "<identifier>":
                if(peek() == "("){
                    Name();
                    read("(");
                    Expression();
                    int count = 2;
                    while(nextToken.type == ","){
                        read(",");
                        Expression();
                        count++;
                    }
                    read(")");

                    constructTree("call", count);
                }else{
                    Name();
                }
                break;
            default:
                System.out.println("ERROR WHILE PARSING: " + nextToken.type); throw new Error();
        }

    }

    void read(String type){
        if(nextToken.type != type){
            System.out.println("EXPECTED: ->|"+type+"|<-");
            System.out.println("FOUND: "+nextToken.type+" "+nextToken.text);
            throw new Error();
        }

        if(hasNext()){
            getNextToken();
        }
    }

    void read(SyntaxKind kind){

        if(nextToken.kind != kind){
            System.out.println("EXPECTED: "+kind);
            System.out.println("FOUND: "+nextToken.kind+" "+nextToken.text);
            throw new Error();
        }

//        ASTNode node_1 = new ASTNode(nextToken.type);
//
//        ASTNode node_2 = new ASTNode(nextToken.text);
//        node_1.addChildNode(node_2);
//
//        treeStack.push(node_1);

        BinaryTreeNode node_1 = new BinaryTreeNode(nextToken.type);
        BinaryTreeNode node_2 = new BinaryTreeNode(nextToken.text);

        node_1.setLeftChild(node_2);

        node_1.setChildCount(1);
        binaryTreeStack.push(node_1);

        getNextToken();

    }


//    void constructTree(String node_label, int count){
//        ASTNode node = new ASTNode(node_label);
//        for(int i = 0; i < count ;i++){
//            node.addChildAtIndex(0,treeStack.pop());
//        }
//        treeStack.push(node);
//    }

    void constructTree(String node_label, int count){
        BinaryTreeNode node = new BinaryTreeNode(node_label);
        BinaryTreeNode p = null;

        for(int j = 0; j < count; j++){
            BinaryTreeNode c = binaryTreeStack.pop();
            if(p != null){
                c.setRightChild(p);
            }
            p = c;
        }
        node.setLeftChild(p);
        node.setChildCount(count);
        binaryTreeStack.push(node);
    }
}

// binary tree node to represent nodes in AST
class BinaryTreeNode{

    private String node_label;

    private BinaryTreeNode left;

    private BinaryTreeNode right;

    private int childCount;

    BinaryTreeNode(String node_label){
        this.node_label = node_label;
    }

    public void setLeftChild(BinaryTreeNode node) {
        left = node;
    }

    public void setRightChild(BinaryTreeNode node) {
        right = node;
    }

    public void setChildCount(int c) {
        childCount = c;
    }

    // Pre Order Traverse with indented printing
    public void PreOrderTraverse(int indentSize){
        for(int i = 0 ; i < indentSize; i++ ) System.out.print(". ");
        System.out.print(this.node_label);
        System.out.println("("+ childCount +")");

        if(this.left != null){
            left.PreOrderTraverse(indentSize + 1);
        }

        if(this.right != null){
            right.PreOrderTraverse(indentSize);
        }
    }
}







/*
 *       vm options : -Xss40m
 *          compile : javac winzigc.java
 *              run : java Program
 *  compile and run : javac winzigc.java; java winzigc -ast winzig_test_programs/winzig_01
 *         test all : javac winzigc.java; java winzigc -test
 */

import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class winzigc {

    private static List<SyntaxToken> tokenStream = new ArrayList<>();

    public static void main(String[] args) {
        String flag = args[0];
        switch (flag){
            case  "-ast":
                String path_to_winzig_program = args[1];
//                System.out.println("generate AST for "+ path_to_winzig_program);
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

                List<SyntaxToken> screenedTokenStream = screenTokenStream();
//                for(SyntaxToken token: screenedTokenStream){
//                    System.out.format("%-30s%5s%20s%15s", token.kind, token.position, token.type, token.text+"\n");
//                }

                Parser parser = new Parser(screenedTokenStream);

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

//                    for(SyntaxToken token: screenTokenStream()){
//                        System.out.format("%-30s%5s%20s%15s", token.kind, token.position, token.type, token.text+"\n");
//                    }
                    Parser p = new Parser(screenTokenStream());
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

}

class Parser{

    private ASTNode rootNode;

    private List<SyntaxToken> tokenStream;

    private int tokenIndex;

    SyntaxToken nextToken;

    Parser(List<SyntaxToken> tokenStream){
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

        consume("program");
        Name(rootNode);
        consume(":");
        Consts(rootNode);
        Types(rootNode);
        Dclns(rootNode);
        SubProgs(rootNode);
        Body(rootNode);
        Name(rootNode);
        consume(".");

//        System.out.println(nextToken.type);

        rootNode.DFTraverse(0);
    }

    void Name(ASTNode parent){
        consume(SyntaxKind.IdentifierToken, parent);
    }

    void Consts(ASTNode parent){
        if(nextToken.type == "const"){
            ASTNode constsNode = addASTNode(parent, "consts");
            consume("const");
            int list_count = 1;
            Const(constsNode);
            while(nextToken.type != ";"){
                consume(",");
                Const(constsNode);
            }
            consume(";");
        }else{
            addASTNode(parent, "consts");
        }
    }

    void Const(ASTNode parent){
        Name(parent);
        consume("=");
        ConstValue(parent);
    }

    void ConstValue(ASTNode parent){
        // skip <char> or <integer> but create a node for <identifier>
        if(nextToken.type.equals("<char>") || nextToken.type.equals("<integer>") ){
            consume(nextToken.kind ,parent);
        }

        if(nextToken.type == "<identifier>"){
            Name(parent);
        }
    }

    void Types(ASTNode parent){
        if(nextToken.type == "type"){
            ASTNode typesNode = addASTNode(parent, "types");
            consume("type");

            while(nextToken.type == "<identifier>"){
                Type(typesNode);
                consume(";");
            }
        }else{
            ASTNode typesNode = addASTNode(parent, "types");
        }
    }

    void Type(ASTNode parent){
        ASTNode typeNode = new ASTNode("type");
        parent.addChildNode(typeNode);

        if(nextToken.type == "<identifier>"){
            consume(SyntaxKind.IdentifierToken, typeNode);
            consume("=");
            LitList(typeNode);
        }
    }

    void LitList(ASTNode parent){
        ASTNode litNode = new ASTNode("lit");
        parent.addChildNode(litNode);

        consume("(");
        Name(litNode);
        while(nextToken.type != ")"){
            consume(",");
            Name(litNode);
        }
        consume(")");
    }

    void Dclns(ASTNode parent){
        if(nextToken.type == "var"){
            ASTNode dclnsNode = addASTNode(parent, "dclns");
            consume("var");
            Dcln(dclnsNode);
            consume(";");
            while(nextToken.type == "<identifier>"){
                Dcln(dclnsNode);
                consume(";");
            }
        }else{
            addASTNode(parent, "dclns");
        }
    }

    void Dcln(ASTNode parent){
        ASTNode varNode = addASTNode(parent, "var");
        Name(varNode);
        while(nextToken.type != ":"){
            consume(",");
            Name(varNode);
        }
        consume(":");
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
        consume("function");
        Name(fcnNode);
        consume("(");
        Params(fcnNode);
        consume(")");
        consume(":");
        Name(fcnNode);
        consume(";");
        Consts(fcnNode);
        Types(fcnNode);
        Dclns(fcnNode);
        Body(fcnNode);
        Name(fcnNode);
        consume(";");
    }

    void Params(ASTNode parent){
        ASTNode paramsNode = addASTNode(parent, "params");
        Dcln(paramsNode);
        while(nextToken.type == ";"){
            consume(";");
            Dcln(paramsNode);
        }
    }

    void Body(ASTNode parent){
        ASTNode blockNode = addASTNode(parent, "block");
        consume("begin");
        Statement(blockNode);
        while(nextToken.type == ";"){
            consume(";");
            Statement(blockNode);
        }
        consume("end");

    }

    void Statement(ASTNode parent){

        switch(nextToken.type){
            case "if":
                ASTNode ifNode = addASTNode(parent, "if");
                consume("if");
                Expression(ifNode);
                consume("then");
                Statement(ifNode);
                if(nextToken.type == "else"){
                    consume("else");
                    Statement(ifNode);
                }
                break;
            case "for":
                ASTNode forNode = addASTNode(parent, "for");
                consume("for");
                consume("(");
                ForStat(forNode);
                consume(";");
                ForExp(forNode);
                consume(";");
                ForStat(forNode);
                consume(")");
                Statement(forNode);
                break;
            case "while":
                ASTNode whileNode = addASTNode(parent, "while");
                consume("while");
                Expression(whileNode);
                consume("do");
                Statement(whileNode);
                break;
            case "repeat":
                ASTNode repeatNode = addASTNode(parent, "repeat");
                consume("repeat");
                Statement(repeatNode);
                while(nextToken.type == ";"){
                    consume(";");
                    Statement(repeatNode);
                }
                consume("until");
                Expression(repeatNode);
                break;
            case "loop":
                ASTNode loopNode = addASTNode(parent, "loop");
                consume("loop");
                Statement(loopNode);
                while(nextToken.type == ";"){
                    consume(";");
                    Statement(loopNode);
                }
                consume("pool");
                break;
            case "output":
                ASTNode outputNode = addASTNode(parent, "output");
                consume("output");
                consume("(");
                OutEXp(outputNode);
                // out exp list
                while(nextToken.type == ","){
                    consume(",");
                    OutEXp(outputNode);
                }
                consume(")");
                break;
            case "exit":
                ASTNode exitNode = addASTNode(parent, "exit");
                consume("exit");
                break;
            case "return":
                ASTNode returnNode = addASTNode(parent, "return");
                consume("return");
                Expression(returnNode);
                break;
            case "read":
                ASTNode readNode = addASTNode(parent, "read");
                consume("read");
                consume("(");
                Name(readNode);
                while(nextToken.type == ","){
                    consume(",");
                    Name(readNode);
                }
                consume(")");
                break;
            case "case":
                ASTNode caseNode = addASTNode(parent, "case");
                consume("case");
                Expression(caseNode);
                consume("of");
                Caseclauses(caseNode);
                OtherwiseClause(caseNode);
                consume("end");
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
        consume(";");
        while(nextToken.type == "<integer>" || nextToken.type == "<char>" || nextToken.type == "<identifier>"){
            Caseclause(parent);
            consume(";");
        }
    }

    void Caseclause(ASTNode parent){
        ASTNode case_clauseNode = addASTNode(parent, "case_clause");
        CaseExpression(case_clauseNode);
        while(nextToken.type == ","){
            consume(",");
            CaseExpression(case_clauseNode);
        }
        consume(":");
        Statement(case_clauseNode);
    }

    void CaseExpression(ASTNode parent){
        ConstValue(parent);
        if(nextToken.type == ".."){
            ASTNode doubleDot = addASTNode(parent, "..");
            consume("..");
            doubleDot.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
            ConstValue(doubleDot);
        }
    }

    void OtherwiseClause(ASTNode parent){
        if(nextToken.type == "otherwise"){
            ASTNode otherwiseNode = addASTNode(parent, "otherwise");
            consume("otherwise");
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
        consume(SyntaxKind.StringToken, parent);
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
                consume(":=");
                Expression(assignNode);
                break;
            case ":=:":
                ASTNode swapNode = addASTNode(parent, "swap");
                Name(swapNode);
                consume(":=:");
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
                    consume("<=");
                    ASTNode lessOrEqNode = addASTNode(parent, "<=");
                    lessOrEqNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(lessOrEqNode);
                    break;
                case "<":
                    consume("<");
                    ASTNode lessNode = addASTNode(parent, "<");
                    lessNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(lessNode);
                    break;
                case ">=":
                    consume(">=");
                    ASTNode greaterOrEqNode = addASTNode(parent, ">=");
                    greaterOrEqNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(greaterOrEqNode);
                    break;
                case ">":
                    consume(">");
                    ASTNode greaterNode = addASTNode(parent, ">");
                    greaterNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(greaterNode);
                    break;
                case "=":
                    consume("=");
                    ASTNode equalNode = addASTNode(parent, "=");
                    equalNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(equalNode);
                    break;
                case "<>":
                    consume("<>");
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
        if (nextToken.type == "+" || nextToken.type == "-" || nextToken.type == "or") {
            switch (nextToken.type) {
                case "+":
                    consume("+");
                    ASTNode addNode = addASTNode(parent, "+");
                    addNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(addNode);
                    break;
                case "-":
                    consume("-");
                    ASTNode minusNode = addASTNode(parent, "-");
                    minusNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(minusNode);
                    break;
                case "or":
                    consume("or");
                    ASTNode orNode = addASTNode(parent, "or");
                    orNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Term(orNode);
                    break;
                default:
                    System.out.println("ERROR in Term");
                    throw new Error();
            }
        }
    }

    void Factor(ASTNode parent){
        Primary(parent);
        if(nextToken.type == "*" || nextToken.type == "/" ||nextToken.type == "and" ||nextToken.type == "mod" ){
            switch(nextToken.type){
                case "*":
                    consume("*");
                    ASTNode mulNode = addASTNode(parent, "*");
                    mulNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(mulNode);
                    break;
                case "/":
                    consume("/");
                    ASTNode divNode = addASTNode(parent, "/");
                    divNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(divNode);
                    break;
                case "and":
                    consume("and");
                    ASTNode andNode = addASTNode(parent, "and");
                    andNode.addChildAtIndex(0, parent.deleteASTNode(parent.getChildNodesCount()-2));
                    Factor(andNode);
                    break;
                case "mod":
                    consume("mod");
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
                consume(SyntaxKind.CharToken,parent); break;
            case "<integer>":
//                System.out.println(nextToken.type + " "+nextToken.text);
                consume(SyntaxKind.IntegerToken,parent); break;
            case "eof":
                addASTNode(parent, "eof");
                consume("eof"); break;
            case "-":
                consume("-");
                ASTNode minusNode = addASTNode(parent, "-");
                Primary(minusNode);
                break;
            case "+":
                consume("+");
                Primary(parent);
                break;
            case "not":
                consume("not");
                ASTNode notNode = addASTNode(parent, "not");
                Primary(notNode);
                break;
            case "(":
                consume("(");
                Expression(parent);
                consume(")");
                break;
            case "succ":
                consume("succ");
                consume("(");
                ASTNode succNode = addASTNode(parent, "succ");
                Expression(succNode);
                consume(")");
                break;
            case "pred":
                consume("pred");
                consume("(");
                ASTNode predNode = addASTNode(parent, "pred");
                Expression(predNode);
                consume(")");
                break;
            case "chr":
                consume("chr");
                consume("(");
                ASTNode chrNode = addASTNode(parent, "chr");
                Expression(chrNode);
                consume(")");
                break;
            case "ord":
                consume("ord");
                consume("(");
                ASTNode ordNode = addASTNode(parent, "ord");
                Expression(ordNode);
                consume(")");
                break;
            case "<identifier>":
                if(peek() == "("){
                    ASTNode callNode = addASTNode(parent, "call");
                    Name(callNode);
                    consume("(");
                    Expression(callNode);
                    while(nextToken.type == ","){
                        consume(",");
                        Expression(callNode);
                    }
                    consume(")");
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

    void consume(String type){
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

    void consume(SyntaxKind kind, ASTNode parent){

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






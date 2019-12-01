import java.util.List;
import java.util.Stack;

public class ParserBottomUpTree extends Parser{
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
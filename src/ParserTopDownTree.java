import java.util.List;

public
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

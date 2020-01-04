public class SyntaxToken {
    SyntaxKind kind;
    int position;
    String text;
    String type;
    int line;
    int column;

    public SyntaxToken(SyntaxKind kind, int position, String text, String type, int line, int column){
        this.kind = kind;
        this.position = position;
        this.text = text;
        this.type = type;
        this.line = line;
        this.column = column;
    }
}
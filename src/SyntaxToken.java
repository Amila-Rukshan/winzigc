public class SyntaxToken {
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
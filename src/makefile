JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./winzigc.java \
	./Lexer.java \
	./SyntaxKind.java \
	./SyntaxToken.java \
	./Parser.java \
	./ParserBottomUpTree.java \
	./ParserTopDownTree.java \
	./ASTNode.java \
	./BinaryTreeNode.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
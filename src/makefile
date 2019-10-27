JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
	$(JC) $(JFLAGS) $*.java

CLASSES = ./winzigc.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class


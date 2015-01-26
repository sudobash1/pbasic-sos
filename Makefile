
JAVAC=javac
sources = $(wildcard sos/*.java)
classes = $(sources:.java=.class)

all: $(classes)

run: all
	java sos.Sim
clean :
	rm -f sos/*.class

%.class : %.java
	$(JAVAC) $<

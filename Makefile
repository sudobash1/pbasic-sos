
JAVAC=javac
sources = $(wildcard sos/*.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f sos/*.class

%.class : %.java
	$(JAVAC) $<

# Makefile to compile the C code to an object, then build a library which can be loaded from that
# This makefile is for macOS only

CC = gcc
CFLAGS = -Wall -pedantic -ansi 
SCRIPTNAME = JavaJNICode_Terminal
CLIBRARY = ../libterminal.dylib

#   Compile the C code and put it into a library that can be loaded, according to https://www.baeldung.com/jni
#   This is all very macos specific, I will need to add support for other platforms later
#   Could also look into this as an alternative https://github.com/nyholku/purejavacomm/blob/master/src/jtermios/Termios.java

$(CLIBRARY) : $(SCRIPTNAME).c $(SCRIPTNAME).h
	$(CC) -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin $(SCRIPTNAME).c -o $(SCRIPTNAME).o
	$(CC) -dynamiclib -o $(CLIBRARY) $(SCRIPTNAME).o -lc

clean :
	rm -f $(SCRIPTNAME).o
#	rm -f $(CLIBRARY)
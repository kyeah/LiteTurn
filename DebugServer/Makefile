###########################################################
# Project 1 Makefile

CC = g++-4.9
CFLAGS = -O3 -std=c++1y
#CFLAGS = -Wall -ggdb -g
#CFLAGS = -g
INCLUDE =
LIBDIR = 

LIBS = -lboost_system -lssl -lcrypto -lglut -lGLU -lGL -lm -ljpeg -lpthread

###########################################################
# Options if compiling on Mac
UNAME := $(shell uname)
ifeq ($(UNAME), Darwin)
CC = g++
CFLAGS = -Wall -g -D__MAC__
INCLUDE = -I/opt/local/include
LIBDIR = -L/lusr/X11/lib -L/opt/local/lib
LIBS = -framework OpenGL -framework GLUT -ljpeg
endif

###########################################################
# Uncomment the following line if you are using Mesa
#LIBS = -lglut -lMesaGLU -lMesaGL -lm

viewer: main.o
	${CC} ${CFLAGS} $(INCLUDE) -o viewer main.o ${LIBDIR} ${LIBS}

main.o: main.cpp
	${CC} ${CFLAGS} -c -o main.o $(INCLUDE) main.cpp 

clean:
	rm -f viewer *.o

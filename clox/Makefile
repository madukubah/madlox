CC = gcc
CFLAGS = -Wall -Wextra -std=c11 -pedantic -ggdb

SOURCES = main.c chunk.h chunk.c memory.h memory.c debug.h debug.c value.h value.c vm.h vm.c compiler.h compiler.c scanner.h scanner.c object.h object.c
OBJECTS = $(SOURCES:.c=.o)

TARGET = main

all: $(TARGET)

$(TARGET): $(SOURCES)
	$(CC) $(CFLAGS) -o $@ $^

clean:
	rm -f $(OBJECTS) $(TARGET)
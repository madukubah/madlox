CC = gcc
CFLAGS = -Wall -Wextra -std=c11 -pedantic -ggdb

SOURCES = main.c chunk.c memory.c debug.c value.c vm.c
OBJECTS = $(SOURCES:.c=.o)

TARGET = main

all: $(TARGET)

$(TARGET): $(SOURCES)
	$(CC) $(CFLAGS) -o $@ $^

clean:
	rm -f $(OBJECTS) $(TARGET)
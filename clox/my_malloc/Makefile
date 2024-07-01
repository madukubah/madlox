CC = gcc
CFLAGS = -Wall -Wextra -std=c11 -pedantic -ggdb

SOURCES = main.c heap.c
OBJECTS = $(SOURCES:.c=.o)

TARGET = heap

all: $(TARGET)

$(TARGET) : $(SOURCES)
	$(CC) $(CFLAGS) -o $@ $^

# $(TARGET): $(SOURCES)
# 	$(CC) $(CFLAGS) -o $@ $^

clean:
	rm -f $(OBJECTS) $(TARGET)
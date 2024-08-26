#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
    OP_CONSTANT, //0
    OP_NIL, //1
    OP_TRUE, //2
    OP_FALSE, //3
    OP_EQUAL, //4
    OP_GREATER, //5
    OP_LESS, //6
    OP_ADD, //7
    OP_SUBTRACT, //8
    OP_MULTIPLY, //9
    OP_DIVIDE, //10
    OP_NOT, //11
    OP_NEGATE, //12
    OP_PRINT,//13
    OP_POP,//14
    OP_DEFINE_GLOBAL,//15
    OP_SET_GLOBAL,//16
    OP_GET_GLOBAL,//17
    OP_SET_LOCAL,//18
    OP_GET_LOCAL,//19
    OP_SET_UPVALUE,//20
    OP_GET_UPVALUE,//21
    OP_CLOSE_UPVALUE,//22
    OP_JUMP_IF_FALSE,//23
    OP_JUMP,//24
    OP_LOOP,//25
    OP_CALL,//26
    OP_CLOSURE,//27
    OP_RETURN,//28
} OpCode;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
    int* lines;
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);

#endif
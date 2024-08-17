#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"
#include "value.h"
#include "table.h"
#include "object.h"

#define FRAME_MAX 64
#define STACK_MAX (FRAME_MAX * UINT8_COUNT)

typedef struct {
    ObjFunction* function;
    uint8_t* ip;
    Value* slots;
} CallFrame;

typedef struct{
    CallFrame frames[FRAME_MAX];
    int frameCount;

    Value stack[STACK_MAX];
    Value* stackTop;
    
    Chunk* chunk;
    uint8_t* ip;
    Table globals;
    Table strings;
    Obj* objects;
} VM;

typedef enum{
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
} InterpretResult;

extern VM vm;

void initVM();
void freeVM();
InterpretResult interpret(char* source);
void push(Value value);
Value pop();

#endif
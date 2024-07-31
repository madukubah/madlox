#include <string.h>

#include "object.h"
#include "memory.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType)\
    (type*)allocateObject(sizeof(type), objectType)

static Obj* allocateObject(size_t size, ObjType type){
    Obj* obj = (Obj*) reallocate(NULL, 0, size);
    obj->type = type;

    obj->next = vm.objects;
    vm.objects = obj;

    return obj;
}

static ObjString* allocateString(const char* chars, int length){
    ObjString* string = (ObjString*) reallocate(NULL, 0, sizeof(ObjString) + (sizeof(char) * (length + 1)));

    string->obj.type = OBJ_STRING;
    string->obj.next = vm.objects;
    vm.objects = (Obj*) string;

    string->length = length;
    memcpy(string->chars, chars, length);
    string->chars[length] = '\0';

    return string;
}

ObjString* copyString(char* chars, int length){
    return allocateString(chars, length);
}

ObjString* takeString(char* chars, int length){
    return allocateString(chars, length);
}


void printObject(Value value){
    switch(OBJ_TYPE(value)){
        case OBJ_STRING:
            printf("%s", AS_CSTRING(value));
            break;
    }
}

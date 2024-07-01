#ifndef HEAP_H
#define HEAP_H

#include <assert.h>
#include <stdint.h>
#include <stdlib.h>

#define UNIMPLEMENTED \
        do { \
            fprintf(stderr, "%s:%d: TODO : %s is not implemented yet \n", __FILE__, __LINE__, __func__); \
            abort(); \
        }while(0)

#define HEAP_CAP_BYTES 640000
static_assert(HEAP_CAP_BYTES % sizeof(uintptr_t) == 0, "the heap capacity is not divisible by the size of the pointer of the platform.");

#define HEAP_CAP_WORDS (HEAP_CAP_BYTES / sizeof(uintptr_t))

extern uintptr_t heap[HEAP_CAP_WORDS];

void *heapAlloc(size_t sizeBytes);
void heapFree(uintptr_t *ptr);
void heapCollect();


#define CHUNK_LIST_CAP 1024

typedef struct {
    uintptr_t *start;
    size_t size;
} Chunk;

typedef struct {
    size_t count;
    Chunk chunks[CHUNK_LIST_CAP];
} ChunkList;

extern ChunkList allocedChunks;
extern ChunkList freedChunks;
extern ChunkList tmpChunks;

int chunkListFind(const ChunkList *list, uintptr_t *ptr);
void chunkListInsert(ChunkList *list, uintptr_t *ptr, size_t size);
void chunkListRemove(ChunkList *list, size_t index);
void chunkListMerge(ChunkList *dst, const ChunkList *src);
void chunkListDump(const ChunkList *list);

#endif // HEAP_H

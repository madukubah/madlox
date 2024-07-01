#include <stdio.h>
#include "./heap.h"

uintptr_t heap[HEAP_CAP_WORDS] = {0};

ChunkList allocedChunks = {0};
ChunkList freedChunks = {
    .count = 1,
    .chunks = {
        [0] = {.start = heap, .size=sizeof(heap)}
    }
};
ChunkList tmpChunks = {0};

int chunkListFind(const ChunkList *list, uintptr_t *ptr){
    for (size_t i = 0; i < list->count; i++)
    {
        if (list->chunks[i].start == ptr)
        {
            return (int)i;
        }
    }
    return -1;
}

void chunkListInsert(ChunkList *list, uintptr_t *ptr, size_t size){
    assert(list->count <= CHUNK_LIST_CAP);
    list->chunks[list->count].start = ptr;
    list->chunks[list->count].size = size;

    for (size_t i = list->count; i > 0 && list->chunks[i].start < list->chunks[i - 1].start ; i--)
    {
        const Chunk t = list->chunks[i];
        list->chunks[i] = list->chunks[i-1];
        list->chunks[i-1] = t;
    }
    
    list->count++;
}

void chunkListRemove(ChunkList *list, size_t index){
    assert(index < list->count);

    for (size_t i = index; i < list->count - 1; i++)
    {
        list->chunks[i] = list->chunks[i + 1];
    }
    list->count -= 1;
}

void chunkListMerge(ChunkList *dst, const ChunkList *src){
    dst->count = 0;
    for (size_t i = 0; i < src->count; i++)
    {
        const Chunk chunk = src->chunks[i];
        if (dst->count > 0)
        {
            Chunk *topChunk = &dst->chunks[dst->count - 1];
            if (topChunk->start + topChunk->size == chunk.start)
            {
                topChunk->size += chunk.size;
            }else{
                chunkListInsert(dst, chunk.start, chunk.size);
            }
        }else{
            chunkListInsert(dst, chunk.start, chunk.size);
        }
    }
}

void chunkListDump(const ChunkList *list){
    printf("Chunks (%zu): \n", list->count);
    for (size_t i = 0; i < list->count; i++)
    {
        printf("  start: %p, size: %zu \n", (void *)list->chunks[i].start, list->chunks[i].size);
    }
}


void *heapAlloc(size_t sizeBytes){
    size_t sizeWords = (sizeBytes + sizeof(uintptr_t) - 1) / sizeof(uintptr_t);
    if (sizeWords > 0)
    {
        chunkListMerge(&tmpChunks, &freedChunks);
        freedChunks = tmpChunks;

        for (size_t i = 0; i < freedChunks.count; i++)
        {
            const Chunk chunk = freedChunks.chunks[i];
            if (chunk.size >= sizeWords)
            {
                chunkListRemove(&freedChunks, i);

                const size_t tailSizeWords = chunk.size - sizeWords;
                chunkListInsert(&allocedChunks, chunk.start, sizeWords);
                if (tailSizeWords > 0)
                {
                    chunkListInsert(&freedChunks, chunk.start + sizeWords, tailSizeWords);
                }
                
                return chunk.start;
            }
        }
    }
    return NULL;
}

void heapFree(uintptr_t *ptr){
    if (ptr != NULL)
    {
        const int index = chunkListFind(&allocedChunks, ptr);
        assert(index >= 0);
        chunkListInsert(&freedChunks, allocedChunks.chunks[index].start, allocedChunks.chunks[index].size);
        chunkListRemove(&allocedChunks, (size_t) index);
    }
}

void heapCollect(){
    UNIMPLEMENTED;
}

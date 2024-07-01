#include <assert.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdint.h>

#include "./heap.h"

#define JIM_IMPLEMENTATION
#include "jim.h"

typedef struct Node Node;

struct Node {
    char x;
    void *left;
    void *right;
};

Node *generateTree(size_t level_cur, size_t level_max){
    if (level_cur < level_max)
    {
        Node *root = (Node *)heapAlloc(sizeof(*root));
        assert((char)level_cur - 'a' <= 'z');
        root->x = level_cur + 'a';
        root->left = generateTree(level_cur + 1, level_max);
        root->right = generateTree(level_cur + 1, level_max);
        return root;
    }else{
        return NULL;
    }
    
}

void printTree(Node *root, Jim *jim);

void printTree(Node *root, Jim *jim){
    (void) root;
    (void) jim;
    // if(root != NULL){
    //     jim_object_begin(jim);
    //     jim_member_key(jim, "value");
    //     jim_string_sized(jim, &root->x, 1);

    //     jim_member_key(jim, "left");
    //     printTree(root->left, jim);

    //     jim_member_key(jim, "right");
    //     printTree(root->right, jim);

    //     jim_object_end(jim);
    // }else{
    //     jim_null(jim);
    // }
}

void func(void *ptr){
    (void) ptr;
}

#define N 10

void *ptrs[N] = {0};

int main(){
    
    stackBase = (const uintptr_t*)__builtin_frame_address(0);
    printf("stackBase : %p\n", (void *)stackBase);
    printf("HEAP : %p\n", (void *)heap);

    for (int i = 0; i < N; i++)
    {
        heapAlloc(i); // will be mark as garbase b`cus its not beaing used
    }

    void *ptr = (void *)heapAlloc(10);
    (void) ptr; // used

    printf("ptr : %p\n", (void *)ptr);


    Node *root = generateTree(0, 3);
    printf("ROOT : %p\n", (void *)root);
    
    Jim jim = {
        .sink = stdout,
        .write = (Jim_Write) fwrite,
    };
    printTree(root, &jim); //used
    
    printf("\n--------------------------------------------\n");
    heapCollect();
    chunkListDump(&allocedChunks, "Alloced");
    chunkListDump(&freedChunks, "Freed");

    printf("\n--------------------------------------------\n");
    root = NULL;
    heapCollect();
    chunkListDump(&allocedChunks, "Alloced");
    chunkListDump(&freedChunks, "Freed");

    
    return 0;
}

// int main2(){

//     // for (int i = 0; i < N; i++)
//     // {
//     //     ptrs[i] = heapAlloc(i);
//     // }

//     // for (int i = 0; i < N; i++)
//     // {
//     //     if (i % 2 == 0)
//     //     {
//     //         heapFree((uintptr_t *)ptrs[i]);
//     //     }
        
//     // }
//     // heapAlloc(10);
//     // // for (int i = 0; i < 10; i++)
//     // // {
//     // //     if (i % 2 == 0)
//     // //     {
//     // //         heapAlloc(i);
//     // //     }
        
//     // // }
    
//     // chunkListDump(&allocedChunks);
//     // chunkListDump(&freedChunks);
//     stackBase = (const uintptr_t *)__builtin_frame_address(0);

//     Node *root = generateTree(0, 3);
//     printf("ROOT : %p\n", (void *)root);
//     Jim jim = {
//         .sink = stdout,
//         .write = (Jim_Write) fwrite,
//     };
//     printTree(root, &jim);
    
//     printf("\n--------------------------------------------\n");

//     size_t heap_ptrs_count = 0;
//     for (size_t i = 0; i < allocedChunks.count; i++)
//     {
//         for (size_t j = 0; j < allocedChunks.chunks[i].size; j++)
//         {
//             printf("ij = %d, %d", i, j);
//             uintptr_t *p = (uintptr_t *) allocedChunks.chunks[i].start[j];
//             printf(" POINTER : %p\n", p);
//             if( p >= heap && p < heap + HEAP_CAP_WORDS){
//                 printf("DETECTED HEAP POINTER : %p\n", p);
//                 heap_ptrs_count++;
//             }
//         }
//     }
//     printf("Detected %zu pointers\n", heap_ptrs_count);

//     for (size_t i = 0; i < 10; i++)
//     {
//         printf("HEAP POINTER : %p\n", &heap[i]);
//     }
    
//     return 0;
// }

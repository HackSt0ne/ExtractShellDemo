#include <jni.h>
#include <string>
#include <unistd.h>
#include <android/log.h>
#include <fcntl.h>
#include <asm/fcntl.h>
#include <sys/mman.h>
#include <dlfcn.h>
#include <dirent.h>

#include "dlfcn/dlfcn_compat.h"
#include "hook/dobby.h"

#define TAG "st0ne"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

struct DexFile {
    // Field order required by test "ValidateFieldOrderOfJavaCppUnionClasses".
    // The class we are a part of.
    uint32_t declaring_class_;
    // Access flags; low 16 bits are defined by spec.
    void *begin;
    /* Dex file fields. The defining dex file is available via declaring_class_->dex_cache_ */
    // Offset to the CodeItem.
    uint32_t size;
};
struct ArtMethod {
    // Field order required by test "ValidateFieldOrderOfJavaCppUnionClasses".
    // The class we are a part of.
    uint32_t declaring_class_;
    // Access flags; low 16 bits are defined by spec.
    uint32_t access_flags_;
    /* Dex file fields. The defining dex file is available via declaring_class_->dex_cache_ */
    // Offset to the CodeItem.
    uint32_t dex_code_item_offset_;
    // Index into method_ids of the dex file associated with this method.
    uint32_t dex_method_index_;
};

void* g_old_execve;
void *(*oriloadmethod)(void *, void *, void *, void *, void *) = nullptr;

int my_execve(const char *filename, char *const argv[], char *const envp[]){
    LOGE("my_execve called, filename=%s", filename);
    return 0;
}

void* my_LoadMethod(void* this_ptr,
                   DexFile* dex_file,
                   void* it,
                   void* klass,
                   ArtMethod* dst) {
    void* ret = oriloadmethod(this_ptr, dex_file, it, klass,dst);
//    LOGE("method id=%d loaded", dst->dex_method_index_);

    if(dst->dex_method_index_ == 30047){
        unsigned char raw_ins[]  ={0x1A, 0x00 , 0x25 , 0xFF , 0x1A , 0x01 , 0xB8 , 0x90 , 0x71 , 0x20 , 0xFF , 0x08 , 0x10 , 0x00 , 0x0E , 0x00};
        int result = mprotect(dex_file->begin, dex_file->size, PROT_WRITE);
        unsigned char *code_item_addr = (unsigned char *) dex_file->begin + dst->dex_code_item_offset_+16;
//        LOGE("code: ");
//        for (int i = 0; i < 16; ++i) {
//            LOGE("%02x", code_item_addr[i]);
//        }

        memcpy(code_item_addr, raw_ins, 16);

//        for (int i = 0; i < 16; ++i) {
//            LOGE("%02x", code_item_addr[i]);
//        }
    }

    return ret;
}

void hook_libc(){
    void* libc = dlopen_compat("libc.so", RTLD_NOW);
    void* execve_addr = dlsym_compat(libc, "execve");
    DobbyHook(execve_addr, (void*)my_execve, &g_old_execve);
//    execv("fuck", 0);
}

void hook_libart(){
    //_ZN3art11ClassLinker10LoadMethodERKNS_7DexFileERKNS_21ClassDataItemIteratorENS_6HandleINS_6mirror5ClassEEEPNS_9ArtMethodE
    void* liart = dlopen_compat("libart.so", RTLD_NOW);
    void* loadmethod_addr = dlsym_compat(liart, "_ZN3art11ClassLinker10LoadMethodERKNS_7DexFileERKNS_21ClassDataItemIteratorENS_6HandleINS_6mirror5ClassEEEPNS_9ArtMethodE");
    DobbyHook(loadmethod_addr, (void*)my_LoadMethod, (void**)&oriloadmethod);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_extractshell_MainActivity_hook(
        JNIEnv* env,
        jobject /* this */) {

    hook_libc();
    hook_libart();
}
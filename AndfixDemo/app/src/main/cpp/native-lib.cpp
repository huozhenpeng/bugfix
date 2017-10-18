#include <jni.h>

#include <dlfcn.h>
#include <string>
#include "dalvik.h"

//android输出日志使用
#define LOG_TAG "fork"
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef Object *(*FindObject)(void *thread, jobject jobject1);
typedef  void* (*FindThread)();
FindObject  findObject;
FindThread  findThread;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_huozhenpeng_andfixdemo_MainActivity_fixFromJNI(JNIEnv *env, jobject instance,
                                                                jint sdk, jobject wrongMethod,
                                                                jobject rightMethod) {
//找到虚拟机对应的Method 结构体
    Method *wrong = (Method *) env->FromReflectedMethod(wrongMethod);

    Method *right =(Method *) env->FromReflectedMethod(rightMethod);


    void *dvm_hand=dlopen("libdvm.so", RTLD_NOW);
//  sdk  10以前是这样  10以后会发生变化
    findObject= (FindObject) dlsym(dvm_hand, sdk > 10 ?
                                             "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject" :
                                             "dvmDecodeIndirectRef");
    findThread = (FindThread) dlsym(dvm_hand, sdk > 10 ? "_Z13dvmThreadSelfv" : "dvmThreadSelf");

// method   所声明的Class
    jclass methodClaz = env->FindClass("java/lang/reflect/Method");
    jmethodID rightMethodId = env->GetMethodID(methodClaz, "getDeclaringClass",
                                               "()Ljava/lang/Class;");
//
    jobject ndkObject = env->CallObjectMethod(rightMethod, rightMethodId);
    ClassObject *firstFiled = (ClassObject *) findObject(findThread(), ndkObject);
    firstFiled->status=CLASS_INITIALIZED;
    wrong->accessFlags |= ACC_PUBLIC;
    wrong->methodIndex=right->methodIndex;
    wrong->jniArgInfo=right->jniArgInfo;
    wrong->registersSize=right->registersSize;
    wrong->outsSize=right->outsSize;
//    方法参数 原型
    wrong->prototype=right->prototype;
//
    wrong->insns=right->insns;
    wrong->nativeFunc=right->nativeFunc;

}





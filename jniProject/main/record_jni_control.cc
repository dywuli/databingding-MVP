#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
// #include <boost/core/ignore_unused.hpp>
#ifdef __cplusplus
extern "C"
{
#endif
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

     const char *TAG = "jni_recorder";
     static const char *className = "voice/example/com/memoryapplication/Test";
     static void init(JNIEnv *env, jobject object)
     {
     }
     static void startRecord(JNIEnv *env, jobject object, jstring jFileName)
     {
          const char *cFileName = env->GetStringUTFChars(jFileName, NULL);
          env->ReleaseStringUTFChars(jFileName, cFileName);
     }
     static void stopRecord(JNIEnv *env, jobject object)
     {
     }
     static jlong getFileSize(JNIEnv *env, jobject object)
     {
          return 2;
     }
     static JNINativeMethod gJniMethods[] = {
         {"init", "()V", (void *)init},
         {"startRecord", "(Ljava/lang/String;)V", (void *)startRecord},
         {"stopRecord", "()V", (void *)stopRecord},
         {"getFileSize", "()J", (void *)getFileSize},

     };
     static int registerNativeMethods(JNIEnv *env, const char *className, const JNINativeMethod *methods, int numberMethods)
     {
          jclass clazz;
          __android_log_print(ANDROID_LOG_DEBUG,"registerNativeMethods","");
          __android_log_print(ANDROID_LOG_DEBUG,className,"");
          clazz = env->FindClass(className);
          if (clazz == NULL)
          {
                __android_log_print(ANDROID_LOG_DEBUG,"registerNativeMethods2","");
               return -1;
          }
          if (env->RegisterNatives(clazz, methods, numberMethods) < 0)
          {
               __android_log_print(ANDROID_LOG_DEBUG,"registerNativeMethods3","");
               return -1;
          }
          env->DeleteLocalRef(clazz);
          return 0;
          
     }

     JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
     {
          JNIEnv *env = NULL;
          jint result = -1;
          if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK)
          {
               return result;
          }
          registerNativeMethods(env, className, gJniMethods, 4);
          return JNI_VERSION_1_4;
     }
#ifdef __cplusplus
}
#endif
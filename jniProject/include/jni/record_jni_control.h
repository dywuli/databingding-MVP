
#include <jni.h>
#ifndef INCLUD_RECORD_JNI_CONTROL_H_
#define INCLUD_RECORD_JNI_CONTROL_H_
#ifdef __cplusplus
extern "C"
{
#endif
     JNIEXPORT void JNICALL
     java_record_jni_control_init(JNIEnv *, jobject);
     JNIEXPORT void JNICALL
     java_record_jni_control_start(JNIEnv *, jobject, jstring);
     JNIEXPORT void JNICALL
     java_record_jni_control_stop(JNIEnv *, jobject);
     JNIEXPORT jstring JNICALL
     java_record_jni_control_getFileSize(JNIEnv *, jobject);
#ifdef __cplusplus
}
#endif

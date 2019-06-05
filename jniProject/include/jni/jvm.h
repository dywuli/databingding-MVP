#include<jni.h>
#ifndef __cpluscplus
extern "C"
{
#endif
#ifndef INCLUDE_JNI_JVM_H_
#define INCLUDE_JNI_JVM_H_
namespace recorder
{
class Jvm
{
private:
     JavaVM *javaVM = nullptr;
     jobject gJobject;
public:
     Jvm(/* args */);
     ~Jvm();
};

Jvm::Jvm(/* args */)
{
}

Jvm::~Jvm()
{
}

}
#ifndef __cpluscplus
}
#endif
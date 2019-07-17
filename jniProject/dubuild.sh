#!/usr/bin/env bash
typeset -r G_INSTALL_DIR=`pwd`/target
typeset -r ANDROID_NDK_PATH=`pwd`/../../SDS/ndk/
typeset -r G_OS='android'
typeset G_ARCH
typeset G_API_LEVEL
CROSS_TOOLCHAIN=/usr
G_ARCH='x86'
G_API_LEVEL=19
G_TOOLCHAIN_FILE=${ANDROID_NDK_PATH}/build/cmake/android.toolchain.cmake 
 #设置编译环境x86 arm64-v8a armeabi-v7a
 #设置ndk 编译jni so 
 #设置install 目录
 #-DCMAKE_TOOLCHAIN_FILE=${G_TOOLCHAIN_FILE} \
  G_BUILD_TYPE="Release"
cmake \
      -DANDROID_ABI=${G_ARCH} \
      -DARCH=${G_ARCH} \
      -DANDROID_STL=c++_shared                   \
      -DANDROID_TOOLCHAIN=clang                  \
      -DANDROID_PLATFORM=android-${G_API_LEVEL}  \
      -DCMAKE_INSTALL_PREFIX=${G_INSTALL_DIR}    
make -j4
make install

cmake_minimum_required (VERSION 2.6)
project (Test)
message(STATUS "CMAKE_SOURCE_DIR->" ${CMAKE_SOURCE_DIR})
message(STATUS "PROJECT_SOURCE_DIR->)" ${PROJECT_SOURCE_DIR})
set (Test_VERSION_MAJOR 1)
set (Test_VERSION_MINOR 0)
set(PATH_LIBS ${CMAKE_SOURCE_DIR}/sources/libs)
set(PATH_CORE ${PROJECT_SOURCE_DIR}/sources/core)
set(ALL_LIBS CACHE INTERNAL "All libs to be linked in this project" )

message(STATUS "PATH_LIBS->" ${PATH_LIBS})
set(PATH_DRAGONBALL ${PROJECT_SOURCE_DIR}/sources/dragonball/android/${ARCH})
set(PATH_SDK_LIBS ${PATH_LIBS}/external_sdk/android/${ARCH}/)
set(PATH_SQLITE_LIBS ${PATH_LIBS}/sqlite/android/${ARCH}/)
set(PATH_BOOST_LIBS ${PATH_LIBS}/boost/android/${ARCH}/)
set(PATH_CURLCPP_LIBS ${PATH_LIBS}/curlcpp/android/${ARCH}/)
message(STATUS "PATH_SDK_LIBS->" ${PATH_SDK_LIBS})
message(STATUS "PATH_BOOST_LIBS->" ${PATH_BOOST_LIBS})
#link_directories 相当于设置环境变量，接下来就可以直接引用lib
link_directories(${PATH_LIBS} ${PATH_SDK_LIBS} ${PATH_SQLITE_LIBS} ${PATH_BOOST_LIBS} ${PATH_CURLCPP_LIBS} ${PATH_DRAGONBALL}/lib/)

set(INC_DIR
    ${PATH_DRAGONBALL}/include/
    ${PROJECT_SOURCE_DIR}/include
    ${PATH_CORE}/inc/
    )
#添加头文件
include_directories (
	"${PROJECT_BINARY_DIR}"
	INC_DIR
	)
#添加libs前提是前边使用了link_directories，例如boost_atomic就直接到相应的PATH_BOOST_LIBS路径去找
set(BOOST_LIBS
    boost_atomic
    #boost_date_time
    #boost_log
    boost_program_options
    #boost_stacktrace_basic
    boost_system
    boost_chrono
    boost_filesystem
    #boost_log_setup
    boost_regex
    #boost_stacktrace_noop
    boost_thread
    )
set(CIMG_LIBS
    jpeg
    png12
    z
    )
list(APPEND ALL_LIBS z log)
list(APPEND ALL_LIBS 
     ${BOOST_LIBS} 
     sqlite3pp 
     sqlite3 
     mobvoi_common_static
     mobvoisds
     mobvoidsp
     curl
     ssl
     crypto
     dl
     curlcpp
    )


# ======================================================================
#                                Sub Directories
# ======================================================================
#add_subdirectory(${PATH_CORE})

aux_source_directory(main DIR_SRCS)
set(TEST_MATH ${DIR_SRCS})
#set(ANDROID_LIBS_SRC ./src/record_jni_control.cc)
#add_library(record SHARED ${ANDROID_LIBS_SRC})

message(STATUS "ALL_LIBS->" ${ALL_LIBS})
add_executable (Test ${TEST_MATH})
install (TARGETS Test DESTINATION ./bin)
#install (FILES "${PROJECT_BINARY_DIR}/TestConfig.h" DESTINATION ./target/include)
#target_link_libraries(record ${ALL_LIBS})
#INSTALL (TARGETS record LIBRARY DESTINATION ./libs)

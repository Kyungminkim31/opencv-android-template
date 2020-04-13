cmake_minimum_required(VERSION 3.4.1)

# 프로젝트 절대경로
set(pathPROJECT /users/kkim/Project/opencv-android-template/)
# opencv 모듈 경로
set(pathOPENCV ${pathPROJECT}/opencv)
# opencv 프로세서 별 라이브러리 경로
set(pathLIBOPENCV_JAVA ${pathOPENCV}/native/libs/${ANDROID_ABI}/libopencv_java4.so)

set(CMAKE_VERBOSE_MAKEFILE on)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathOPENCV}/native/jni/include)

include_directories(src/main/cpp/include/)

# jni 에 사용될 라이브러리
add_library( native-lib SHARED ${pathPROJECT}/app/src/main/cpp/native-lib.cpp)

add_library( lib_opencv SHARED IMPORTED )

set_target_properties( lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.

        log)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(
        native-lib
        lib_opencv

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )



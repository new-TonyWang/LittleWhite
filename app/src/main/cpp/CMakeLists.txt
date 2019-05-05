# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, se ets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

aux_source_directory(source SRC_FILE)
aux_source_directory(include include_FILE)
add_library( # Sets the name of the library.
        cbinarizer
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${SRC_FILE}
        ${include_FILE}
        ${CMAKE_SOURCE_DIR}
        cbinarizer.cpp
        CBinarize.cpp
        CBinarize.h
        )
add_library( # Sets the name of the library.
        opencv

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).


        IMPORTED

        )
aux_source_directory(opencv2 opencv2_File)
#include_directories(${SRC_FILE})
include_directories(include)
include_directories(source)
include_directories(opencv2)
#add_subdirectory(./ ./build)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    set_target_properties(
            opencv
            PROPERTIES IMPORTED_LOCATION
            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libopencv_java4.so
    )
endif (${ANDROID_ABI} STREQUAL "arm64-v8a")

if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    set_target_properties(
            opencv
            PROPERTIES IMPORTED_LOCATION
            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libopencv_java4.so
    )
endif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        cbinarizer
        opencv
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

add_library(

        jxffmpegrun


        # Sets the library as a shared library.

        SHARED


        # Provides a relative path to your source file(s).

        cmdutils.c

        ffmpeg.c

        ffmpeg_filter.c

        ffmpeg_opt.c

        jx_ffmpeg_cmd_run.c

)

add_library(

        avcodec

        SHARED

        IMPORTED

)


add_library(

        avfilter

        SHARED

        IMPORTED

)


add_library(

        avformat

        SHARED

        IMPORTED

)


add_library(

        avutil

        SHARED

        IMPORTED

)


add_library(

        swresample

        SHARED

        IMPORTED

)


add_library(

        swscale

        SHARED

        IMPORTED

)


add_library(

        fdk-aac

        SHARED

        IMPORTED

)


if (${ANDROID_ABI} STREQUAL "armeabi-v7a")


    set_target_properties(

            avcodec

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libavcodec.so

    )


    set_target_properties(

            avfilter

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libavfilter.so

    )


    set_target_properties(

            avformat

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libavformat.so

    )


    set_target_properties(

            avutil

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libavutil.so

    )


    set_target_properties(

            swresample

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libswresample.so

    )


    set_target_properties(

            swscale

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libswscale.so

    )


    set_target_properties(

            fdk-aac

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libfdk-aac.so

    )


endif (${ANDROID_ABI} STREQUAL "armeabi-v7a")


if (${ANDROID_ABI} STREQUAL "arm64-v8a")

    set_target_properties(

            avcodec

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libavcodec.so

    )


    set_target_properties(

            avfilter

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libavfilter.so

    )


    set_target_properties(

            avformat

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libavformat.so

    )


    set_target_properties(

            avutil

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libavutil.so

    )


    set_target_properties(

            swresample

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libswresample.so

    )


    set_target_properties(

            swscale

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libswscale.so

    )


    set_target_properties(

            fdk-aac

            PROPERTIES IMPORTED_LOCATION

            ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libfdk-aac.so

    )

endif (${ANDROID_ABI} STREQUAL "arm64-v8a")


include_directories(

        G:/workspace/AndroidStudioProjects/LittleWhite/ffmpeg-3.2.5
)

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


target_link_libraries( # Specifies the target library.

        jxffmpegrun

        fdk-aac

        avcodec

        avfilter

        avformat

        avutil

        swresample

        swscale

        # Links the target library to the log library

        # included in the NDK.

        ${log-lib})

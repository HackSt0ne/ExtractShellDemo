# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("extractshell")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        dlfcn/dlfcn_compat.c
        dlfcn/dlfcn_nougat.c
        native-lib.cpp)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        dobby)

#引入静态库
#       ① 参数 1 ( add ) : 设置引入的静态库名称
#       ② 参数 2 ( SHARED ) : 设置引入的函数库类型 : ① 静态库 STATIC ② 动态库 SHARED
#       ③ 参数 3 ( IMPORTED ) : 表示引入第三方静态库 , 导入静态库 , 相当于预编译静态库
#                                   后续还需要设置导入路径 , 配合该配置使用
add_library(
    # 设置引入的静态库名称
    dobby

    # 设置引入的函数库类型为静态库
    STATIC

    # 表示引入第三方静态库
    IMPORTED)

# 设置上述静态库的导入路径
#       设置目标属性参数 :
#           ① 参数 1 ( add ) : 要设置哪个函数库的属性
#           ② 参数 2 ( PROPERTIES ) : 设置目标属性
#           ③ 参数 3 ( IMPORTED_LOCATION ) : 设置导入路径
#           ④ 参数 4 : 配置静态库的文件路径
set_target_properties(
    # 设置目标
    dobby

    # 设置属性
    PROPERTIES

    # 导入路径
    IMPORTED_LOCATION

    # ${CMAKE_SOURCE_DIR} 是本 CMakeList.txt 构建脚本的路径 , 是 CMake 工具内置的变量
    #       Android CMake 也内置了一些变量 , 如 ANDROID_ABI
    ${CMAKE_SOURCE_DIR}/hook/libdobby.a)
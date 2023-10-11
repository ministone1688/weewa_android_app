# version required
cmake_minimum_required(VERSION 3.5)

# our library name
project(weewacrypt)

# compile flags
if(CMAKE_BUILD_TYPE MATCHES "Debug")
#    add_definitions(-DDEBUG)
endif()
add_definitions(-fvisibility=hidden)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti -Wno-error=format-security -fpermissive")

# imported library
set(IMPORT_DIR ${CMAKE_SOURCE_DIR}/../../../libs)
add_library(shared-lib SHARED IMPORTED)
set_target_properties(shared-lib PROPERTIES IMPORTED_LOCATION
        ${IMPORT_DIR}/${ANDROID_ABI}/libsodium.so)

# libraries we need
find_library(log-lib log)

# source files
add_library(
        ${PROJECT_NAME}
        SHARED
        byte_array.cpp
        crypto.cpp
        binding.cpp
)

# include dir
include_directories(
        .
        include
        include/sodium
)

# link
target_link_libraries(
        ${PROJECT_NAME}
        ${log-lib}
        shared-lib
)
cmake_minimum_required(VERSION 3.22.1)

project("vision_native")

add_library(
    vision_native
    SHARED
    vision_native.cpp
)

set_target_properties(vision_native PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")

find_library(log-lib log)

target_link_libraries(
    ${PROJECT_NAME}
    ${log-lib}
)
cmake_minimum_required(VERSION 3.15)
project(ice_example)

# Ścieżki do Ice (dostosuj do swojej lokalizacji)
set(ICE_INCLUDE_DIR "./include")
set(ICE_LIB_DIR "./lib")

# Linkowanie bibliotek
add_executable(main src/main.cpp)
target_include_directories(main PRIVATE ${ICE_INCLUDE_DIR})
target_link_directories(main PRIVATE ${ICE_LIB_DIR})
target_link_libraries(main ice ice++11)


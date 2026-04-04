# Define a versão mínima do CMake (padrão Android)
cmake_minimum_required(VERSION 3.22.1)

# Nome da nossa biblioteca nativa
project("vision_native")

# Adiciona a biblioteca e vincula ao arquivo fonte
add_library(
    vision_native
    SHARED
    vision_native.cpp
)

# Inclui bibliotecas nativas do Android (log para debug e jnigraphics para imagens)
find_library(log-lib log)
find_library(graphics-lib jnigraphics)

# Vincula as bibliotecas ao nosso projeto
target_link_libraries(
    ${PROJECT_NAME}
    ${log-lib}
    ${graphics-lib}
)


cmake_minimum_required(VERSION 2.8)
project(libimgui)

INCLUDE(FindJNI)
INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})

add_library(imgui SHARED
    imconfig.h
    imgui.cpp
    imgui.h
    imgui_demo.cpp
    imgui_draw.cpp
    imgui_internal.h
    imgui_widgets.cpp
    imstb_rectpack.h
    imstb_textedit.h
    imstb_truetype.h
    cn_lambdalib2_vis_editor_ImGui.cpp
    cn_lambdalib2_vis_editor_ImGui.h
)

install(TARGETS imgui DESTINATION lib)

@echo off

if exist build (
    echo "Building binary..."
    cmake --build build\ --config Release

    echo "Deleting old dll..."
    del ..\resources\imgui_64.dll

    echo "Copying dll..."
    copy build\Release\imgui.dll ..\resources\imgui_64.dll
) else (
    echo "build folder doesn't exist."
)

pause

@echo on


@echo off
chcp 65001 >nul
echo 检查DLL依赖关系
echo ==================
echo.

set DLL_DIR=native-libs\windows-x64

echo 关键DLL文件检查:
echo.

if exist "%DLL_DIR%\libirisengine.dll" (
    echo [✓] libirisengine.dll - 核心引擎
) else (
    echo [✗] libirisengine.dll - 缺失！
)

if exist "%DLL_DIR%\irisenginehelper.dll" (
    echo [✓] irisenginehelper.dll - JNI桥接
) else (
    echo [✗] irisenginehelper.dll - 缺失！
)

if exist "%DLL_DIR%\EyeID.dll" (
    echo [✓] EyeID.dll
) else (
    echo [✗] EyeID.dll - 缺失！
)

if exist "%DLL_DIR%\skcamera.dll" (
    echo [✓] skcamera.dll - 相机驱动
) else (
    echo [✗] skcamera.dll - 缺失！
)

if exist "%DLL_DIR%\libsbklic.dll" (
    echo [✓] libsbklic.dll - 授权
) else (
    echo [✗] libsbklic.dll - 缺失！
)

echo.
echo 运行时库检查:
echo.

if exist "%DLL_DIR%\libcrypto-3-x64.dll" (
    echo [✓] libcrypto-3-x64.dll
) else (
    echo [✗] libcrypto-3-x64.dll - 缺失！
)

if exist "%DLL_DIR%\libssl-3-x64.dll" (
    echo [✓] libssl-3-x64.dll
) else (
    echo [✗] libssl-3-x64.dll - 缺失！
)

if exist "%DLL_DIR%\libstdc++-6.dll" (
    echo [✓] libstdc++-6.dll - C++运行时
) else (
    echo [✗] libstdc++-6.dll - 缺失！
)

if exist "%DLL_DIR%\libgcc_s_sjlj-1.dll" (
    echo [✓] libgcc_s_sjlj-1.dll - GCC运行时
) else (
    echo [✗] libgcc_s_sjlj-1.dll - 缺失！
)

if exist "%DLL_DIR%\libwinpthread-1.dll" (
    echo [✓] libwinpthread-1.dll - 线程库
) else (
    echo [✗] libwinpthread-1.dll - 缺失！
)

echo.
echo 全部DLL文件列表:
echo.
dir /B "%DLL_DIR%\*.dll"
echo.
pause


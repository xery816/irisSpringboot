@echo off
chcp 65001 >nul
echo Starting Iris Recognition Service on Windows...

set NATIVE_LIB_DIR=%CD%\native-libs\windows-x64

REM 把DLL目录加入PATH环境变量
set PATH=%NATIVE_LIB_DIR%;%PATH%

mvn spring-boot:run -Djava.library.path="%NATIVE_LIB_DIR%"


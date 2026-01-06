@echo off
chcp 65001 >nul
echo ========================================
echo   Iris Recognition Service (Windows)
echo ========================================
echo.

REM 获取脚本所在目录
set PROJECT_DIR=%~dp0
set NATIVE_LIB_DIR=%PROJECT_DIR%native-libs\windows-x64
set JAR_FILE=%PROJECT_DIR%target\iris-springboot-1.0.0.jar

echo Project directory: %PROJECT_DIR%
echo Native library directory: %NATIVE_LIB_DIR%
echo JAR file: %JAR_FILE%
echo.

REM 检查JAR文件是否存在
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found at %JAR_FILE%
    echo Please build the project first using: mvn clean package -DskipTests
    echo Or copy iris-springboot-1.0.0.jar from target/ to project root
    pause
    exit /b 1
)

REM 检查DLL文件是否存在
if not exist "%NATIVE_LIB_DIR%\irisenginehelper.dll" (
    echo Error: Native libraries not found in %NATIVE_LIB_DIR%
    echo Please ensure all DLL files are in native-libs\windows-x64\
    pause
    exit /b 1
)

REM 添加DLL目录到PATH环境变量（让Windows找到依赖DLL）
set PATH=%NATIVE_LIB_DIR%;%PATH%
echo PATH updated to include native libraries
echo.

REM 切换工作目录到native-libs\windows-x64（让DLL能相对路径访问sound/temp等资源）
echo Changing working directory to: %NATIVE_LIB_DIR%
cd /d "%NATIVE_LIB_DIR%"
echo Current directory: %CD%
echo.

REM 检查Java版本
echo Checking Java version...
java -version
echo.

REM 启动服务
echo Starting Iris Recognition Service...
echo Service will be available at: http://localhost:8084
echo Press Ctrl+C to stop the service
echo.
echo ========================================
echo.

REM 运行JAR（java.library.path指向当前目录）
java -Djava.library.path="." ^
     -Xmx512m ^
     -Xms256m ^
     -jar "%JAR_FILE%"

pause

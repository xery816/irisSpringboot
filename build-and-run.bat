@echo off
chcp 65001 >nul
echo Building and Running Iris Recognition Service...

echo Step 1: Clean and package...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Step 1.5: Preparing native-libs environment...
REM 复制配置文件到根目录（DLL需要在根目录读取）
xcopy /Y src\main\resources\config\*.cfg native-libs\windows-x64\ > nul
REM 确保temp目录存在
if not exist "native-libs\windows-x64\temp" mkdir native-libs\windows-x64\temp
echo Native-libs environment ready.

echo.
echo Step 2: Starting application...
set PROJECT_DIR=%CD%
set NATIVE_LIB_DIR=%PROJECT_DIR%\native-libs\windows-x64
set JAR_FILE=%PROJECT_DIR%\target\iris-springboot-1.0.0.jar

REM 把DLL目录加入PATH环境变量（让Windows找到依赖DLL）
set PATH=%NATIVE_LIB_DIR%;%PATH%

REM 切换工作目录到native-libs\windows-x64（让DLL能相对路径访问sound/temp等资源）
echo Working directory: %NATIVE_LIB_DIR%
echo JAR file: %JAR_FILE%
cd /d "%NATIVE_LIB_DIR%"

REM 启动应用（java.library.path指向当前目录）
java -Djava.library.path="." -jar "%JAR_FILE%"

pause


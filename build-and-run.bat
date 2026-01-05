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
echo Step 2: Starting application...
set JAVA_LIBRARY_PATH=F:\iris_java\Windows SDK_3.23.6_x64_Eng\win_pidsdk_3.23.6_x64\bin

java -Djava.library.path="%JAVA_LIBRARY_PATH%" -jar target\iris-springboot-1.0.0.jar

pause


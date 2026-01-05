@echo off
chcp 65001 >nul
echo Starting Iris Recognition Service on Windows...

set JAVA_LIBRARY_PATH=F:\iris_java\Windows SDK_3.23.6_x64_Eng\win_pidsdk_3.23.6_x64\bin

mvn spring-boot:run -Djava.library.path="%JAVA_LIBRARY_PATH%"


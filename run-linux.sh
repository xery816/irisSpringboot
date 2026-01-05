#!/bin/bash

echo "========================================"
echo "  Iris Recognition Service (Linux)"
echo "========================================"
echo ""

# 获取脚本所在目录的绝对路径
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
NATIVE_LIB_DIR="$PROJECT_DIR/native-libs/linux-x64"
JAR_FILE="$PROJECT_DIR/target/iris-springboot-1.0.0.jar"

echo "Project directory: $PROJECT_DIR"
echo "Native library directory: $NATIVE_LIB_DIR"
echo "JAR file: $JAR_FILE"
echo ""

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first using: mvn clean package -DskipTests"
    exit 1
fi

# 检查SO文件是否存在
if [ ! -f "$NATIVE_LIB_DIR/libirisenginehelper.so" ]; then
    echo "Error: Native libraries not found in $NATIVE_LIB_DIR"
    echo "Please copy all SO files to native-libs/linux-x64/"
    exit 1
fi

# 设置LD_LIBRARY_PATH（让系统能找到SO文件）
export LD_LIBRARY_PATH="$NATIVE_LIB_DIR:$LD_LIBRARY_PATH"
echo "LD_LIBRARY_PATH: $LD_LIBRARY_PATH"
echo ""

# 切换工作目录到native-libs/linux-x64（让程序能相对路径访问配置和资源）
cd "$NATIVE_LIB_DIR" || exit 1
echo "Working directory changed to: $(pwd)"
echo ""

# 检查JDK版本
echo "Checking Java version..."
java -version
echo ""

# 启动服务
echo "Starting Iris Recognition Service..."
echo "Service will be available at: http://localhost:8080"
echo "Press Ctrl+C to stop the service"
echo ""
echo "========================================"
echo ""

# 运行JAR（java.library.path指向当前目录）
exec java -Djava.library.path="." \
     -Xmx512m \
     -Xms256m \
     -jar "$JAR_FILE"

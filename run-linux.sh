#!/bin/bash

echo "========================================"
echo "  Iris Recognition Service (Linux)"
echo "========================================"
echo ""

# 获取脚本所在目录的绝对路径
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
NATIVE_LIB_DIR="$PROJECT_DIR/native-libs/linux-x64"
JAR_FILE="$PROJECT_DIR/iris-springboot-1.0.0.jar"

echo "Project directory: $PROJECT_DIR"
echo "Native library directory: $NATIVE_LIB_DIR"
echo "JAR file: $JAR_FILE"
echo ""

# 自动设置权限
echo "Setting file permissions..."
chmod +x "$0" 2>/dev/null || true
chmod +x "$NATIVE_LIB_DIR"/*.so 2>/dev/null || true
echo "Permissions updated"
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

# 切换工作目录到native-libs/linux-x64（让程序能相对路径访问配置和资源）
cd "$NATIVE_LIB_DIR" || exit 1
echo "Working directory changed to: $(pwd)"

# 设置LD_LIBRARY_PATH（让系统能找到SO文件）
# 必须在cd之后，使用当前目录
export LD_LIBRARY_PATH="$(pwd):$LD_LIBRARY_PATH"
echo "LD_LIBRARY_PATH: $LD_LIBRARY_PATH"
echo ""

# 检查JDK版本
echo "Checking Java version..."
java -version
echo ""

# 启动服务
echo "Starting Iris Recognition Service..."
echo "Service will be available at: http://localhost:8084"
echo "Press Ctrl+C to stop the service"
echo ""
echo "========================================"
echo ""

# 运行JAR（java.library.path指向当前目录）
# 显式设置LD_LIBRARY_PATH和LD_PRELOAD
export LD_LIBRARY_PATH="$(pwd):$LD_LIBRARY_PATH"
export LD_PRELOAD="$(pwd)/libirisengine.so:$(pwd)/libsbklic.so:$(pwd)/libsbkutil.so"
echo "LD_PRELOAD: $LD_PRELOAD"
echo ""

exec env LD_LIBRARY_PATH="$(pwd):$LD_LIBRARY_PATH" \
        LD_PRELOAD="$(pwd)/libirisengine.so:$(pwd)/libsbklic.so:$(pwd)/libsbkutil.so" \
    java -Djava.library.path="." \
         -Xmx512m \
         -Xms256m \
         -jar "$JAR_FILE"

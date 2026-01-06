# 虹膜识别系统 Spring Boot 服务

基于Spring Boot的虹膜识别REST API服务，支持Windows和Linux平台。

## 快速开始

### Windows

```bash
# 1. 编译（仅首次或代码更新后需要）
mvn clean package -DskipTests

# 2. 运行
.\build-and-run.bat
```

### Linux

```bash
# 1. 编译
mvn clean package -DskipTests

# 2. 运行
chmod +x run-linux.sh
./run-linux.sh
```

## 测试

打开浏览器访问 `test-client.html` 或直接访问：
- 服务地址: http://localhost:8084
- 视频流: http://localhost:8084/api/stream/mjpeg

## 主要功能

- ✅ 设备初始化
- ✅ 用户注册（虹膜采集）
- ✅ 用户识别
- ✅ 用户管理（查询、删除）
- ✅ 实时视频预览（MJPEG流）
- ✅ 设备信息查询
- ✅ 硬件检测

## API端点

### 设备控制
- `POST /api/iris/init` - 初始化设备
- `POST /api/iris/stop` - 停止操作
- `GET /api/iris/device/info` - 设备信息
- `POST /api/iris/check-hardware` - 硬件检测

### 用户管理
- `POST /api/iris/enroll?userId=xxx` - 注册用户
- `POST /api/iris/identify` - 识别用户
- `GET /api/iris/users` - 用户列表
- `DELETE /api/iris/users/{userId}` - 删除用户

### 视频流
- `GET /api/stream/mjpeg` - MJPEG视频流

## 项目结构

```
iris_springboot/
├── src/main/java/cn/simbok/iris/
│   ├── controller/          # REST控制器
│   ├── service/             # 业务逻辑
│   ├── model/               # 数据模型
│   ├── config/              # 配置类
│   └── util/                # 工具类
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   └── config/              # SDK配置文件
├── target/                  # Maven构建输出
│   └── iris-springboot-1.0.0.jar
├── native-libs/             # 原生库
│   ├── windows-x64/         # Windows DLL
│   └── linux-x64/           # Linux SO
├── build-and-run.bat        # Windows构建+启动脚本
├── run-linux.sh             # Linux启动脚本
├── test-client.html         # 测试客户端
└── DEPLOY.txt               # 详细部署文档
```

**部署说明：**
- **Windows**: 保持target目录结构，直接运行 build-and-run.bat
- **Linux**: 将 target/iris-springboot-1.0.0.jar 复制到根目录与native-libs平级
```

## 数据存储

用户虹膜数据存储在 `native-libs/{platform}/temp/data/eyedata/` 目录：

```
eyedata/
├── user001/
│   ├── left.dat      # 左眼特征
│   ├── left.jpg      # 左眼图片
│   ├── right.dat     # 右眼特征
│   └── right.jpg     # 右眼图片
└── user002/
    └── ...
```

## 技术栈

- Spring Boot 2.7.18
- JDK 1.8
- Maven 3.x
- 虹膜识别SDK 3.23.6

## 注意事项

1. **工作目录**: 应用必须在 `native-libs/{platform}` 目录下运行
2. **预览显示**: 预览视频仅在执行注册/识别操作时显示
3. **图像旋转**: 预览图像已自动旋转180度
4. **端口配置**: 默认8084端口，可通过 `--server.port=9090` 修改

## 详细文档

查看 `DEPLOY.txt` 获取完整的部署和配置说明。

## 许可证

专有软件，未经授权不得使用。


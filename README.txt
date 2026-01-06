虹膜识别系统 - SpringBoot REST API版
=======================================

项目特点
-------
✓ 跨平台支持 (Windows + Linux)
✓ REST API接口
✓ MJPEG实时视频流
✓ 完全独立部署
✓ 前后端分离架构


快速开始
-------
1. copy-all-dependencies.bat  (首次必须)
2. CHECK_DEPENDENCIES.bat     (检查)
3. build-and-run.bat          (运行)

详见: QUICK_START.txt


主要功能
-------
- 设备初始化
- 用户注册
- 虹膜识别
- 用户管理
- 实时预览
- 硬件检测
- 固件更新
- 配置管理


技术栈
-----
- Spring Boot 2.7.18
- Java 8+
- Maven
- JNI (Native Interface)
- REST API
- MJPEG Streaming


API接口
-------
http://localhost:8084

基础接口:
- POST /api/iris/init
- POST /api/iris/enroll
- POST /api/iris/identify
- GET  /api/iris/users
- DELETE /api/iris/user/{id}

高级接口:
- GET  /api/iris/advanced/device-info
- POST /api/iris/advanced/check-hardware
- POST /api/iris/advanced/firmware
... 更多详见 API.txt

视频流:
- GET  /stream/preview (MJPEG)


目录说明
-------
native-libs/      - JNI库和音频文件
src/              - Java源代码
temp/             - 运行时数据
test-client.html  - Web测试客户端


文档清单
-------
QUICK_START.txt   - 快速开始（推荐）
STANDALONE.txt    - 独立部署详解
API.txt           - 完整API文档
DEPLOY.txt        - 部署指南
CHANGES.txt       - 功能清单


技术支持
-------
开发商: 北京思搏克智能科技有限公司
SDK版本: 3.23.6
Java SDK: 1.2
SpringBoot版本: 1.0.0


版权声明
-------
本项目基于思搏克虹膜识别SDK开发
仅供学习和研究使用


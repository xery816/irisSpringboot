Linux x64 Native Libraries
===========================

本目录包含Linux平台需要的.so文件：

核心库：
-------
libirisengine.so       - 虹膜识别引擎核心库
libirisenginehelper.so - Java JNI桥接库

依赖检查：
---------
在Linux上运行前，检查依赖：
  ldd libirisengine.so

可能需要安装：
  sudo apt-get install libssl-dev
  sudo apt-get install libportaudio2

部署路径建议：
------------
/opt/iris_springboot/native-libs/linux-x64/

使用说明：
---------
从 pidsdk-java-v1.2/libs/ubuntu-x64/ 复制文件


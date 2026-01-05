Windows x64 Native Libraries
==============================

本目录包含所有Windows平台需要的DLL文件：

核心库：
--------
libirisengine.dll       - 虹膜识别引擎核心库
irisenginehelper.dll    - Java JNI桥接库
EyeID.dll              - 眼部识别库

相机驱动：
---------
skcamera.dll           - 虹膜相机驱动

授权和工具：
-----------
libsbklic.dll          - 授权验证库
libsbkutil.dll         - 工具库
libxuctl.dll           - 控制库

加密库：
-------
libcrypto-3-x64.dll    - OpenSSL加密库
libssl-3-x64.dll       - OpenSSL SSL库

网络库：
-------
ntp.dll                - 网络时间协议

音频库：
-------
libportaudio.dll       - 音频处理库

MinGW运行时：
------------
libatomic-1.dll        - 原子操作库
libgcc_s_sjlj-1.dll    - GCC运行时
libstdc++-6.dll        - C++标准库
libwinpthread-1.dll    - POSIX线程库

音频资源：
---------
sound/chinese/         - 中文语音提示
sound/english/         - 英文语音提示

使用说明：
---------
运行 copy-all-dependencies.bat 自动复制所有文件


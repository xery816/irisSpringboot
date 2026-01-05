====================================================================
Linux 原生库文件说明
====================================================================

当前状态：不完整
----------------
本目录当前只包含 JNI 封装库 (libirisenginehelper.so)，
缺少核心 SDK 库文件。

需要补充的文件：
---------------
请从 Linux PID SDK 3.23.6 或更高版本中复制以下文件：

必需的 SO 文件：
  ├── libirisenginehelper.so    ✓ (已存在)
  ├── libirisengine.so           ✗ (需要)
  ├── libsbklic.so               ✗ (需要)
  ├── libsbkutil.so              ✗ (需要)
  ├── libxuctl.so                ✗ (需要)
  ├── libskcamera.so             ✗ (需要)
  └── [其他依赖库]               ✗ (需要)

配置文件：
  ├── param_common.cfg           ✗ (需要)
  └── param_dev.cfg              ✗ (需要)

资源目录：
  ├── sound/                     ✗ (需要)
  │   ├── chinese/
  │   │   ├── alarm.wav
  │   │   ├── enroll.wav
  │   │   ├── identify.wav
  │   │   └── [其他音频文件]
  │   └── english/
  │       └── [相同音频文件]
  └── temp/                      ✗ (需要)
      ├── data/
      │   ├── captured/
      │   ├── eyedata/
      │   └── snapshot/
      └── log/

获取方式：
---------
1. 从 SDK 提供商获取 Linux 版本安装包
   文件名可能类似：Linux_pidsdk_3.23.6_x64.tar.gz

2. 解压后，从 SDK 的 lib/ 目录复制所有 .so 文件

3. 从 SDK 的 bin/ 或 config/ 目录复制配置文件

4. 复制 sound/ 目录的完整内容

5. 创建 temp/ 目录结构（可以为空）

验证方法：
---------
运行以下命令检查依赖关系：

ldd libirisenginehelper.so

应该显示所有依赖库都能找到，例如：
  linux-vdso.so.1 => (0x...)
  libirisengine.so => ./libirisengine.so (0x...)
  libsbklic.so => ./libsbklic.so (0x...)
  libstdc++.so.6 => /usr/lib/x86_64-linux-gnu/libstdc++.so.6 (0x...)
  libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x...)
  ...

如果显示 "not found"，说明缺少依赖库。

部署前检查清单：
--------------
[ ] 所有 SO 文件已复制
[ ] param_common.cfg 已复制
[ ] param_dev.cfg 已复制
[ ] sound/ 目录已复制
[ ] temp/ 目录已创建
[ ] ldd 检查通过，无 "not found"
[ ] SO 文件权限设置正确 (chmod +x *.so)

技术支持：
---------
如需获取完整的 Linux SDK，请联系：
北京赛博克智能科技有限公司
或查阅 SDK 开发指南文档

====================================================================

====================================================================
Linux 原生库文件说明
====================================================================

当前状态：完整 ✓
----------------
本目录已包含 Linux PID SDK 3.23.6 的所有必需文件。

文件清单：
---------
JNI 封装库：
  ✓ libirisenginehelper.so    (JNI封装库)

核心SDK库：
  ✓ libirisengine.so           (虹膜识别引擎)
  ✓ libsbklic.so               (许可管理)
  ✓ libsbkutil.so              (工具库)
  ✓ libxuctl.so                (设备控制)
  ✓ libskcamera.so             (相机驱动)
  ✓ libeyeid.so                (眼睛识别)
  ✓ libntp.so                  (网络时间协议)
  ✓ libportaudio.so            (音频输出)

系统依赖库：
  ✓ libcrypto.so / libcrypto.so.3   (加密库)
  ✓ libssl.so / libssl.so.3         (SSL库)

配置文件：
  ✓ param_common.cfg           (通用配置)
  ✓ param_dev.cfg              (设备配置)

资源目录：
  ✓ sound/                     (音频资源)
      ├── chinese/             (中文语音)
      └── english/             (英文语音)
  ✓ temp/                      (临时文件和数据)
      ├── data/
      │   ├── captured/        (采集图片)
      │   ├── eyedata/         (用户虹膜数据)
      │   └── snapshot/        (快照)
      └── log/                 (日志文件)

部署验证：
---------
在Linux系统上运行以下命令检查依赖：

ldd libirisenginehelper.so

预期结果：所有依赖库都应该被解析，没有 "not found"

运行服务：
---------
1. 确保JDK 1.8+已安装
2. 配置USB设备权限（如需要）
3. 执行启动脚本：
   chmod +x run-linux.sh
   ./run-linux.sh

4. 或使用systemd服务（推荐生产环境）

系统要求：
---------
- 操作系统: Ubuntu 18.04+ / CentOS 7+ / Debian 9+
- 架构: x86_64 (AMD64)
- JDK: 1.8 或更高版本
- 内存: 至少 512MB 可用内存
- USB: 支持USB 2.0或更高版本

权限设置：
---------
# 设置SO文件执行权限
chmod +x *.so*

# 配置USB设备访问权限（根据实际设备调整）
sudo nano /etc/udev/rules.d/99-iris-device.rules
# 添加: SUBSYSTEM=="usb", ATTRS{idVendor}=="xxxx", MODE="0666"
sudo udevadm control --reload-rules

故障排查：
---------
如果遇到 "cannot open shared object file" 错误：

1. 检查LD_LIBRARY_PATH是否正确设置
2. 运行 ldd 检查缺失的依赖
3. 确认工作目录是 native-libs/linux-x64/
4. 检查文件权限是否正确

如果设备无法识别：
1. 检查 lsusb 输出
2. 确认udev规则已配置
3. 检查用户是否在plugdev组

性能优化：
---------
- 建议使用 -Xmx512m 限制JVM内存
- 生产环境建议使用 systemd 管理服务
- 定期清理 temp/log/ 目录
- 备份 temp/data/eyedata/ 用户数据

更新日期：2026-01-05
SDK版本：PID SDK 3.23.6 for Linux AMD64

====================================================================

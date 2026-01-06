# Linux 部署快速指南

## 一、准备部署包

在Windows开发机上打包：

```bash
cd F:\iris_springboot
mvn clean package -DskipTests
```

## 二、上传到Linux服务器

需要上传以下内容到Linux服务器（例如 `/opt/iris-service`）：

```
/opt/iris-service/
├── target/iris-springboot-1.0.0.jar
├── native-libs/linux-x64/
│   ├── *.so (所有SO文件)
│   ├── param_common.cfg
│   ├── param_dev.cfg
│   ├── sound/
│   └── temp/
├── run-linux.sh
└── iris-service.service (可选)
```

使用scp上传：

```bash
# 打包
cd F:\iris_springboot
tar -czf iris-service.tar.gz target/iris-springboot-1.0.0.jar native-libs/linux-x64 run-linux.sh iris-service.service

# 上传
scp iris-service.tar.gz user@server:/opt/

# 在服务器上解压
ssh user@server
cd /opt
tar -xzf iris-service.tar.gz
mv iris-service.tar.gz iris-service/
```

## 三、配置权限

```bash
cd /opt/iris-service

# 设置脚本执行权限
chmod +x run-linux.sh
chmod +x native-libs/linux-x64/*.so

# 配置USB设备权限
sudo nano /etc/udev/rules.d/99-iris-device.rules
```

添加以下内容（根据实际设备调整idVendor和idProduct）：

```
SUBSYSTEM=="usb", ATTRS{idVendor}=="xxxx", ATTRS{idProduct}=="xxxx", MODE="0666"
```

重新加载udev规则：

```bash
sudo udevadm control --reload-rules
sudo udevadm trigger
```

## 四、测试运行

```bash
cd /opt/iris-service
./run-linux.sh
```

看到以下信息表示启动成功：

```
Tomcat started on port(s): 8084 (http)
Started IrisApplication
```

按 Ctrl+C 停止测试。

## 五、测试API

在另一个终端或浏览器测试：

```bash
# 测试服务健康
curl http://localhost:8084/actuator/health

# 初始化设备
curl -X POST http://localhost:8084/api/iris/init

# 查看用户列表
curl http://localhost:8084/api/iris/users
```

## 六、配置为系统服务（推荐）

### 方式1: systemd服务

```bash
# 编辑服务文件，修改路径
sudo nano iris-service.service

# 复制到systemd目录
sudo cp iris-service.service /etc/systemd/system/

# 重新加载
sudo systemctl daemon-reload

# 启用开机自启
sudo systemctl enable iris-service

# 启动服务
sudo systemctl start iris-service

# 查看状态
sudo systemctl status iris-service

# 查看日志
sudo journalctl -u iris-service -f
```

### 方式2: 后台运行

```bash
# 后台运行
nohup ./run-linux.sh > /var/log/iris-service.log 2>&1 &

# 查看日志
tail -f /var/log/iris-service.log

# 查看进程
ps aux | grep iris-springboot

# 停止服务
kill $(ps aux | grep 'iris-springboot' | grep -v grep | awk '{print $2}')
```

## 七、防火墙配置

如需外网访问，开放8084端口：

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 8084/tcp
sudo ufw reload

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=8084/tcp
sudo firewall-cmd --reload

# 或者使用iptables
sudo iptables -A INPUT -p tcp --dport 8084 -j ACCEPT
sudo service iptables save
```

## 八、验证部署

1. 访问Web界面：
   ```
   http://服务器IP:8084
   ```

2. 测试视频流：
   ```
   http://服务器IP:8084/api/stream/mjpeg
   ```

3. 上传test-client.html到服务器，通过浏览器访问

## 九、故障排查

### 问题1: SO文件找不到

```bash
cd /opt/iris-service/native-libs/linux-x64
ldd libirisenginehelper.so
# 检查是否有 "not found"
```

解决：确保LD_LIBRARY_PATH正确，所有SO文件存在

### 问题2: USB设备无权限

```bash
lsusb  # 查看设备
ls -l /dev/bus/usb/xxx/xxx  # 检查权限
```

解决：配置udev规则或临时修改权限

### 问题3: 端口被占用

```bash
sudo netstat -tulpn | grep 8084
```

解决：修改application.yml中的端口或停止占用端口的进程

### 问题4: 内存不足

```bash
free -h  # 查看内存
```

解决：调整JVM参数或增加服务器内存

## 十、监控和维护

### 查看日志

```bash
# systemd服务
sudo journalctl -u iris-service -f

# 后台运行
tail -f /var/log/iris-service.log

# SDK日志
tail -f /opt/iris-service/native-libs/linux-x64/temp/log/*.log
```

### 数据备份

```bash
# 备份用户数据
tar -czf iris-data-$(date +%Y%m%d).tar.gz \
    /opt/iris-service/native-libs/linux-x64/temp/data/eyedata

# 定期清理日志
find /opt/iris-service/native-libs/linux-x64/temp/log -name "*.log" -mtime +7 -delete
```

### 性能监控

```bash
# CPU和内存使用
top -p $(pgrep -f iris-springboot)

# 网络连接
netstat -an | grep 8084

# 磁盘使用
df -h /opt/iris-service
```

## 十一、常用命令总结

```bash
# 启动服务
sudo systemctl start iris-service

# 停止服务
sudo systemctl stop iris-service

# 重启服务
sudo systemctl restart iris-service

# 查看状态
sudo systemctl status iris-service

# 查看日志
sudo journalctl -u iris-service -f --lines=100

# 重新加载配置（需重启服务）
sudo systemctl daemon-reload
sudo systemctl restart iris-service
```

## 十二、安全建议

1. 修改默认端口8084
2. 配置防火墙，仅允许必要的IP访问
3. 启用HTTPS（配置SSL证书）
4. 定期更新系统和JDK
5. 限制服务运行用户权限（不建议用root）
6. 定期备份用户数据

---

更新日期：2026-01-05


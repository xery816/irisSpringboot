#!/bin/bash

echo "========================================"
echo "  Iris Service Installation Script"
echo "========================================"
echo ""

# 检查是否以root运行
if [ "$EUID" -ne 0 ]; then 
    echo "Error: This script must be run as root"
    echo "Please run: sudo ./install-service.sh"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVICE_FILE="$SCRIPT_DIR/iris-service.service"

# 检查服务文件是否存在
if [ ! -f "$SERVICE_FILE" ]; then
    echo "Error: Service file not found: $SERVICE_FILE"
    exit 1
fi

echo "Found service file: $SERVICE_FILE"
echo ""

# 1. 复制服务文件到systemd目录
echo "Step 1: Copying service file to /etc/systemd/system/"
cp "$SERVICE_FILE" /etc/systemd/system/
if [ $? -eq 0 ]; then
    echo "✓ Service file copied successfully"
else
    echo "✗ Failed to copy service file"
    exit 1
fi
echo ""

# 2. 重新加载systemd
echo "Step 2: Reloading systemd daemon..."
systemctl daemon-reload
if [ $? -eq 0 ]; then
    echo "✓ Systemd daemon reloaded"
else
    echo "✗ Failed to reload systemd daemon"
    exit 1
fi
echo ""

# 3. 启用开机自启
echo "Step 3: Enabling iris-service to start on boot..."
systemctl enable iris-service
if [ $? -eq 0 ]; then
    echo "✓ Service enabled for auto-start"
else
    echo "✗ Failed to enable service"
    exit 1
fi
echo ""

# 4. 启动服务
echo "Step 4: Starting iris-service..."
systemctl start iris-service
if [ $? -eq 0 ]; then
    echo "✓ Service started successfully"
else
    echo "✗ Failed to start service"
    echo ""
    echo "Checking service status..."
    systemctl status iris-service
    exit 1
fi
echo ""

# 等待几秒让服务启动
echo "Waiting for service to initialize..."
sleep 3
echo ""

# 5. 显示服务状态
echo "========================================"
echo "Service Status:"
echo "========================================"
systemctl status iris-service --no-pager -l
echo ""

# 6. 显示最近日志
echo "========================================"
echo "Recent Logs:"
echo "========================================"
journalctl -u iris-service -n 20 --no-pager
echo ""

echo "========================================"
echo "Installation Complete!"
echo "========================================"
echo ""
echo "Useful commands:"
echo "  View logs:       sudo journalctl -u iris-service -f"
echo "  Check status:    sudo systemctl status iris-service"
echo "  Restart service: sudo systemctl restart iris-service"
echo "  Stop service:    sudo systemctl stop iris-service"
echo "  Disable service: sudo systemctl disable iris-service"
echo ""
echo "Service will be available at: http://localhost:8084"
echo ""

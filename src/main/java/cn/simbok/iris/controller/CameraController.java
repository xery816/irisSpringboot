package cn.simbok.iris.controller;

import cn.simbok.iris.model.CameraInfo;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/camera")
@CrossOrigin(origins = "*")
public class CameraController {

    private static final Logger log = LoggerFactory.getLogger(CameraController.class);
    private static final int DEVICE_TIMEOUT_SECONDS = 3; // 单个设备超时时间（给足够时间初始化）
    private static final int MAX_CONCURRENT_TESTS = 1;   // OpenCV不支持并发，改为串行
    private static final int MAX_DEVICE_INDEX = 6;       // 只测试video0-1，通常够用

    /**
     * 列出所有可用摄像头
     * GET /api/camera/list
     */
    @GetMapping("/list")
    public Map<String, Object> listCameras() {
        log.info("[摄像头列表] 开始扫描摄像头...");

        List<CameraInfo> cameras = new ArrayList<>();
        Map<Integer, String> devicePaths = new HashMap<>();

        try {
            // 1. 扫描 /dev/video* 设备（Linux）- 只扫描常用设备避免浪费时间
            File devDir = new File("/dev");
            if (devDir.exists() && devDir.isDirectory()) {
                // 只扫描 video0-3，跳过元数据设备（通常 >3 的是元数据节点）
                for (int i = 0; i <= MAX_DEVICE_INDEX; i++) {
                    File device = new File(devDir, "video" + i);
                    if (device.exists()) {
                        devicePaths.put(i, device.getAbsolutePath());
                    }
                }

                log.info("[摄像头列表] 发现 {} 个设备节点: {}", devicePaths.size(), devicePaths.values());
            } else {
                // Windows 环境，尝试索引 0-3
                log.info("[摄像头列表] 非Linux环境，尝试索引 0-3");
                for (int i = 0; i <= MAX_DEVICE_INDEX; i++) {
                    devicePaths.put(i, "Camera " + i);
                }
            }

            // 2. 并行测试设备（提高速度）
            ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_TESTS);
            List<Future<CameraInfo>> futures = new ArrayList<>();

            for (Map.Entry<Integer, String> entry : devicePaths.entrySet()) {
                final int index = entry.getKey();
                final String devicePath = entry.getValue();

                Future<CameraInfo> future = executor.submit(() -> testCamera(index, devicePath));
                futures.add(future);
            }

            // 收集结果（每个设备独立超时）
            for (int i = 0; i < futures.size(); i++) {
                Future<CameraInfo> future = futures.get(i);
                try {
                    log.debug("[摄像头列表] 等待第{}个设备结果...", i + 1);
                    CameraInfo info = future.get(DEVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (info != null) {
                        cameras.add(info);
                        log.info("[摄像头列表] 成功添加摄像头: {} (共{}个)", info.getDevice(), cameras.size());
                    } else {
                        log.info("[摄像头列表] 第{}个设备不可用", i + 1);
                    }
                } catch (TimeoutException e) {
                    log.warn("[摄像头列表] 第{}个设备测试超时，取消任务", i + 1);
                    future.cancel(true);
                } catch (Exception e) {
                    log.warn("[摄像头列表] 第{}个设备测试异常: {}", i + 1, e.getMessage());
                }
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            log.info("[摄像头列表] 扫描完成，找到 {} 个可用摄像头", cameras.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cameras", cameras);
            return response;

        } catch (Exception e) {
            log.error("[摄像头列表] 扫描失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("cameras", cameras);
            return response;
        }
    }

    /**
     * 测试单个摄像头设备
     * @param index 设备索引
     * @param devicePath 设备路径
     * @return 摄像头信息，如果失败返回null
     */
    private CameraInfo testCamera(int index, String devicePath) {
        long startTime = System.currentTimeMillis();
        log.info("[摄像头列表] 开始检查设备 {}", devicePath);

        OpenCVFrameGrabber grabber = null;
        try {
            // 尝试打开设备
            grabber = new OpenCVFrameGrabber(index);
            grabber.setImageWidth(640);   // 设置较小分辨率加快初始化
            grabber.setImageHeight(480);
            grabber.setTimeout(500);      // 设置500ms读取超时

            grabber.start();

            // 获取设备信息
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            double fps = grabber.getFrameRate();

            // 设备名称
            String cameraName = "Camera " + index;

            CameraInfo info = new CameraInfo(
                index,
                devicePath,
                width,
                height,
                fps > 0 ? fps : 30.0,  // 默认30fps
                cameraName
            );

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[摄像头列表] ✓ {} 可用: {}x{} @{}fps (耗时{}ms)",
                devicePath, width, height, fps, elapsed);

            return info;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[摄像头列表] ✗ {} 无法打开 (耗时{}ms)", devicePath, elapsed);
            return null;
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    // 忽略释放错误
                }
            }
        }
    }
}


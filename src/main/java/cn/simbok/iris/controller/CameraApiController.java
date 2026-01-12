package cn.simbok.iris.controller;

import cn.simbok.iris.service.IrisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 摄像头控制API
 * 兼容旧版Python服务的接口路径
 */
@RestController
@RequestMapping("/api/camera")
@CrossOrigin(origins = "*")
public class CameraApiController {

    private static final Logger log = LoggerFactory.getLogger(CameraApiController.class);

    @Autowired
    private IrisService irisService;

    /**
     * 启动摄像头
     * 实际上就是初始化虹膜设备
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startCamera() {
        log.info("[摄像头] 启动摄像头（初始化设备）");

        Map<String, Object> response = new HashMap<>();

        try {
            int result = irisService.initDevice();
            
            response.put("success", result == 0);
            
            if (result == 0) {
                response.put("message", "Camera started successfully");
                log.info("[摄像头] 启动成功");
            } else {
                String errorMsg = irisService.getErrorMessage(result);
                response.put("error", errorMsg);
                log.error("[摄像头] 启动失败: {}", errorMsg);
            }

        } catch (Exception e) {
            log.error("[摄像头] 启动异常", e);
            response.put("success", false);
            response.put("error", "Camera start failed: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 停止摄像头
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopCamera() {
        log.info("[摄像头] 停止摄像头");

        Map<String, Object> response = new HashMap<>();

        try {
            int result = irisService.stop();
            
            response.put("success", result == 0);
            
            if (result == 0) {
                response.put("message", "Camera stopped successfully");
                log.info("[摄像头] 停止成功");
            } else {
                String errorMsg = irisService.getErrorMessage(result);
                response.put("error", errorMsg);
                log.error("[摄像头] 停止失败: {}", errorMsg);
            }

        } catch (Exception e) {
            log.error("[摄像头] 停止异常", e);
            response.put("success", false);
            response.put("error", "Camera stop failed: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}

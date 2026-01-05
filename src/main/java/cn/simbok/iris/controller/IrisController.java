package cn.simbok.iris.controller;

import cn.simbok.iris.model.ApiResponse;
import cn.simbok.iris.service.IrisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/iris")
@CrossOrigin(origins = "*")
public class IrisController {
    
    private static final Logger log = LoggerFactory.getLogger(IrisController.class);
    
    @Autowired
    private IrisService irisService;
    
    /**
     * 初始化设备
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> init() {
        try {
            int result = irisService.initDevice();
            Map<String, Object> response = new HashMap<>();
            response.put("code", result);
            response.put("success", result == 0);
            response.put("message", result == 0 ? "Init success" : "Init failed: " + irisService.getErrorMessage(result));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Init error", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Init error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 注册用户
     */
    @PostMapping("/enroll")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> enroll(@RequestParam String userId) {
        log.info("Enrolling user: {}", userId);
        
        return irisService.enrollUser(userId).thenApply(message -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", message.contains("成功"));
            response.put("message", message);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {
            log.error("Enroll error", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Enroll error: " + e.getMessage());
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * 识别用户
     */
    @PostMapping("/identify")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> identify(
            @RequestParam(defaultValue = "false") boolean continuous) {
        log.info("Identifying user, continuous: {}", continuous);
        
        return irisService.identifyUser(continuous).thenApply(message -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", message.contains("成功"));
            response.put("message", message);
            
            // 提取用户ID（如果识别成功）
            if (message.contains("成功") && message.contains(":")) {
                String userId = message.substring(message.lastIndexOf(":") + 1).trim();
                response.put("userId", userId);
            }
            
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {
            log.error("Identify error", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Identify error: " + e.getMessage());
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * 停止当前操作
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<Void>> stop() {
        try {
            int result = irisService.stop();
            if (result == 0) {
                return ResponseEntity.ok(ApiResponse.success("Operation stopped", null));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Stop failed: " + irisService.getErrorMessage(result)));
            }
        } catch (Exception e) {
            log.error("Stop error", e);
            return ResponseEntity.ok(ApiResponse.error("Stop error: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<String>>> getUsers() {
        try {
            List<String> users = irisService.getUserList();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Get users error", e);
            return ResponseEntity.ok(ApiResponse.error("Get users error: " + e.getMessage()));
        }
    }
    
    /**
     * 删除指定用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        try {
            int result = irisService.deleteUser(userId);
            if (result == 0) {
                return ResponseEntity.ok(ApiResponse.success("User deleted: " + userId, null));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Delete failed: " + irisService.getErrorMessage(result)));
            }
        } catch (Exception e) {
            log.error("Delete user error", e);
            return ResponseEntity.ok(ApiResponse.error("Delete error: " + e.getMessage()));
        }
    }
    
    /**
     * 删除所有用户
     */
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<Void>> deleteAllUsers() {
        try {
            int result = irisService.deleteAllUsers();
            if (result == 0) {
                return ResponseEntity.ok(ApiResponse.success("All users deleted", null));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Delete all failed: " + irisService.getErrorMessage(result)));
            }
        } catch (Exception e) {
            log.error("Delete all users error", e);
            return ResponseEntity.ok(ApiResponse.error("Delete all error: " + e.getMessage()));
        }
    }
    
    /**
     * 获取设备信息
     */
    @GetMapping("/device/info")
    public ResponseEntity<ApiResponse<String>> getDeviceInfo() {
        try {
            String info = irisService.getDeviceInfo();
            if (info != null) {
                return ResponseEntity.ok(ApiResponse.success(info));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Device info not available"));
            }
        } catch (Exception e) {
            log.error("Get device info error", e);
            return ResponseEntity.ok(ApiResponse.error("Error: " + e.getMessage()));
        }
    }
    
    /**
     * 获取引擎信息
     */
    @GetMapping("/engine/info")
    public ResponseEntity<ApiResponse<String>> getEngineInfo() {
        try {
            String info = irisService.getEngineInfo();
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (Exception e) {
            log.error("Get engine info error", e);
            return ResponseEntity.ok(ApiResponse.error("Error: " + e.getMessage()));
        }
    }
    
    /**
     * 硬件检测
     */
    @PostMapping("/check-hardware")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> checkHardware() {
        log.info("Checking hardware...");
        
        return irisService.checkHardware().thenApply(message -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", message.contains("通过"));
            response.put("message", message);
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {
            log.error("Hardware check error", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Hardware check error: " + e.getMessage());
            return ResponseEntity.ok(response);
        });
    }
}

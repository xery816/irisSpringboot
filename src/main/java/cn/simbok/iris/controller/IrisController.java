package cn.simbok.iris.controller;

import cn.simbok.iris.service.IrisService;
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
    
    @Autowired
    private IrisService irisService;
    
    /**
     * 初始化设备
     */
    @PostMapping("/init")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> init() {
        return irisService.initDevice().thenApply(result -> {
            Map<String, Object> response = new HashMap<>();
            response.put("code", result);
            response.put("success", result == 0);
            response.put("message", result == 0 ? "初始化成功" : "初始化失败");
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * 注册用户
     */
    @PostMapping("/enroll")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> enroll(@RequestParam String userId) {
        return irisService.enrollUser(userId).thenApply(message -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", message.contains("成功"));
            response.put("message", message);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * 识别用户
     */
    @PostMapping("/identify")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> identify(
            @RequestParam(defaultValue = "true") boolean continuous) {
        return irisService.identifyUser(continuous).thenApply(message -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", message.contains("成功"));
            response.put("message", message);
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers() {
        List<String> users = irisService.getUserList();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        int result = irisService.deleteUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result == 0);
        response.put("message", result == 0 ? "删除成功" : "删除失败");
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 停止当前操作
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop() {
        int result = irisService.stop();
        Map<String, Object> response = new HashMap<>();
        response.put("success", result == 0);
        response.put("message", result == 0 ? "停止成功" : "停止失败");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取设备状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("initialized", irisService.isInitialized());
        return ResponseEntity.ok(response);
    }
}


package cn.simbok.iris.controller;

import cn.simbok.iris.model.ApiResponse;
import cn.simbok.iris.service.IrisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iris")
@CrossOrigin(origins = "*")
public class IrisController {
    
    @Autowired
    private IrisService irisService;
    
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> init() {
        int result = irisService.initDevice();
        Map<String, Object> response = new HashMap<>();
        response.put("code", result);
        response.put("success", result == 0);
        response.put("message", result == 0 ? "Init success" : "Init failed");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enroll(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Enroll API placeholder");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/identify")
    public ResponseEntity<Map<String, Object>> identify() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Identify API placeholder");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<String>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<Void>> deleteAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

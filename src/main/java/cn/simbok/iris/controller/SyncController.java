package cn.simbok.iris.controller;

import cn.simbok.iris.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 虹膜数据同步控制器
 * 支持多台设备间的双向数据同步
 */
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private SyncService syncService;

    /**
     * 双向同步接口
     * 接收两个客户端地址，自动完成双向数据同步
     *
     * 请求参数示例:
     * {
     *   "clientA": "192.168.2.100:8084",
     *   "clientB": "192.168.2.101:8084"
     * }
     */
    @PostMapping("/bidirectional")
    public ResponseEntity<Map<String, Object>> syncBidirectional(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            String clientA = request.get("clientA");
            String clientB = request.get("clientB");

            if (clientA == null || clientA.isEmpty() || clientB == null || clientB.isEmpty()) {
                response.put("success", false);
                response.put("error", "缺少客户端地址参数");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("[双向同步] 开始同步: {} <-> {}", clientA, clientB);

            // 判断当前服务是哪个客户端
            String currentHost = httpRequest.getHeader("Host");
            if (currentHost == null) {
                currentHost = httpRequest.getLocalAddr() + ":" + httpRequest.getLocalPort();
            }

            String peerUrl;
            if (currentHost.equals(clientA)) {
                // 当前是客户端A
                peerUrl = "http://" + clientB;
                log.info("[双向同步] 当前是客户端A，对方是客户端B: {}", peerUrl);
            } else if (currentHost.equals(clientB)) {
                // 当前是客户端B
                peerUrl = "http://" + clientA;
                log.info("[双向同步] 当前是客户端B，对方是客户端A: {}", peerUrl);
            } else {
                log.warn("[双向同步] 当前服务地址 {} 不在同步列表中", currentHost);
                response.put("success", false);
                response.put("error", "当前服务地址 " + currentHost + " 不在同步列表中");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行双向同步
            Map<String, Object> syncResult = syncService.performBidirectionalSync(peerUrl);

            return ResponseEntity.ok(syncResult);

        } catch (Exception e) {
            log.error("[双向同步] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 打包本地虹膜数据供下载
     */
    @PostMapping("/package")
    public ResponseEntity<Map<String, Object>> packageData(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("[打包数据] 开始...");

            // 打包数据
            Map<String, Object> result = syncService.packageIrisData();

            if (!(Boolean) result.get("success")) {
                return ResponseEntity.status(500).body(result);
            }

            String filename = (String) result.get("filename");
            long fileSize = (Long) result.get("size");

            // 构造下载URL
            String host = request.getHeader("Host");
            if (host == null) {
                host = request.getLocalAddr() + ":" + request.getLocalPort();
            }
            String downloadUrl = "http://" + host + "/api/sync/download/" + filename;

            response.put("success", true);
            response.put("url", downloadUrl);
            response.put("size", fileSize);
            response.put("filename", filename);

            log.info("[打包数据] 完成，大小: {} MB", fileSize / 1024.0 / 1024.0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[打包数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 下载打包好的虹膜数据文件
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            log.info("[下载文件] 请求文件: {}", filename);

            Resource resource = syncService.getPackageFile(filename);

            if (!resource.exists()) {
                log.error("[下载文件] 文件不存在: {}", filename);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            log.error("[下载文件] 失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 接收对方推送的虹膜数据
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadData(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "上传文件为空");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("[接收数据] 文件: {}, 大小: {} MB",
                    file.getOriginalFilename(),
                    file.getSize() / 1024.0 / 1024.0);

            // 处理上传的数据
            Map<String, Object> result = syncService.receiveAndApplyData(file);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[接收数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取同步状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> status = syncService.getSyncStatus();
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("[获取状态] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

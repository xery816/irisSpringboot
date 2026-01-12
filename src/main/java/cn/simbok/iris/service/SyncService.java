package cn.simbok.iris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 虹膜数据同步服务
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    // 虹膜数据目录（只同步eyedata）
    private static final String IRIS_DATA_DIR = "/opt/iris/data/data/eyedata";
    
    // 临时文件目录
    private static final String TEMP_DIR = "/tmp";

    // 同步状态
    private volatile boolean isSyncing = false;
    private volatile String lastSyncTime = null;
    private volatile String lastSyncResult = null;

    /**
     * 执行双向同步
     * 1. 推送本地数据到对方
     * 2. 从对方拉取数据
     */
    public Map<String, Object> performBidirectionalSync(String peerUrl) {
        Map<String, Object> response = new HashMap<>();

        if (isSyncing) {
            response.put("success", false);
            response.put("error", "同步正在进行中，请稍后再试");
            return response;
        }

        try {
            isSyncing = true;
            log.info("[双向同步] 目标: {}", peerUrl);

            // 步骤1: 推送本地数据到对方
            log.info("[双向同步] 步骤1: 推送本地数据到对方...");
            Map<String, Object> pushResult = pushDataToPeer(peerUrl);

            if (!(Boolean) pushResult.get("success")) {
                response.put("success", false);
                response.put("error", "推送数据失败: " + pushResult.get("error"));
                lastSyncResult = "失败: 推送失败";
                return response;
            }

            // 步骤2: 从对方拉取数据
            log.info("[双向同步] 步骤2: 从对方拉取数据...");
            Map<String, Object> pullResult = pullDataFromPeer(peerUrl);

            if (!(Boolean) pullResult.get("success")) {
                response.put("success", false);
                response.put("error", "拉取数据失败: " + pullResult.get("error"));
                lastSyncResult = "失败: 拉取失败";
                return response;
            }

            response.put("success", true);
            response.put("message", "双向同步完成");
            
            Map<String, Object> details = new HashMap<>();
            details.put("push", pushResult);
            details.put("pull", pullResult);
            response.put("details", details);

            lastSyncTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            lastSyncResult = "成功";

            log.info("[双向同步] 完成");
            return response;

        } catch (Exception e) {
            log.error("[双向同步] 异常", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            lastSyncResult = "失败: " + e.getMessage();
            return response;
        } finally {
            isSyncing = false;
        }
    }

    /**
     * 推送本地数据到对方客户端
     */
    private Map<String, Object> pushDataToPeer(String peerUrl) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 打包本地数据
            File zipFile = new File(TEMP_DIR, "iris_data_push_" + System.currentTimeMillis() + ".zip");
            packIrisData(zipFile);

            long fileSize = zipFile.length();
            log.info("[推送数据] 打包完成，大小: {} MB", fileSize / 1024.0 / 1024.0);

            // 2. 上传到对方
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(zipFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.postForEntity(
                    peerUrl + "/api/sync/upload",
                    requestEntity,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            // 3. 清理临时文件
            zipFile.delete();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                response.put("success", true);
                response.put("message", "推送成功");
                response.put("size", fileSize);
                log.info("[推送数据] 成功");
            } else {
                response.put("success", false);
                response.put("error", "HTTP " + responseEntity.getStatusCodeValue());
            }

            return response;

        } catch (Exception e) {
            log.error("[推送数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 从对方客户端拉取数据
     */
    private Map<String, Object> pullDataFromPeer(String peerUrl) {
        Map<String, Object> response = new HashMap<>();

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. 请求对方打包数据
            log.info("[拉取数据] 请求对方打包数据...");
            ResponseEntity<Map<String, Object>> packageResponse = restTemplate.postForEntity(
                    peerUrl + "/api/sync/package",
                    null,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            if (!packageResponse.getStatusCode().is2xxSuccessful()) {
                response.put("success", false);
                response.put("error", "对方响应异常: " + packageResponse.getStatusCodeValue());
                return response;
            }

            Map<String, Object> packageResult = packageResponse.getBody();
            if (packageResult == null || !(Boolean) packageResult.get("success")) {
                response.put("success", false);
                response.put("error", "对方打包失败");
                return response;
            }

            String downloadUrl = (String) packageResult.get("url");
            log.info("[拉取数据] 下载链接: {}", downloadUrl);

            // 2. 下载ZIP文件
            byte[] zipData = restTemplate.getForObject(downloadUrl, byte[].class);

            if (zipData == null || zipData.length == 0) {
                response.put("success", false);
                response.put("error", "下载数据为空");
                return response;
            }

            File zipFile = new File(TEMP_DIR, "iris_data_pull_" + System.currentTimeMillis() + ".zip");
            Files.write(zipFile.toPath(), zipData);

            long fileSize = zipFile.length();
            log.info("[拉取数据] 下载完成，大小: {} MB", fileSize / 1024.0 / 1024.0);

            // 3. 备份并解压
            String backupDir = backupCurrentData();
            unpackIrisData(zipFile);

            // 4. 清理临时文件
            zipFile.delete();

            response.put("success", true);
            response.put("message", "拉取数据成功");
            response.put("size", fileSize);
            response.put("backup", backupDir);

            log.info("[拉取数据] 成功，备份: {}", backupDir);

            return response;

        } catch (Exception e) {
            log.error("[拉取数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 打包虹膜数据
     */
    public Map<String, Object> packageIrisData() {
        Map<String, Object> response = new HashMap<>();

        try {
            long timestamp = System.currentTimeMillis();
            String filename = "iris_data_" + timestamp + ".zip";
            File zipFile = new File(TEMP_DIR, filename);

            packIrisData(zipFile);

            response.put("success", true);
            response.put("filename", filename);
            response.put("size", zipFile.length());

            return response;

        } catch (Exception e) {
            log.error("[打包数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 获取打包文件
     */
    public Resource getPackageFile(String filename) {
        File file = new File(TEMP_DIR, filename);
        return new FileSystemResource(file);
    }

    /**
     * 接收并应用上传的数据
     */
    public Map<String, Object> receiveAndApplyData(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 保存上传文件
            File tempFile = new File(TEMP_DIR, "iris_data_received_" + System.currentTimeMillis() + ".zip");
            file.transferTo(tempFile);

            long fileSize = tempFile.length();
            log.info("[接收数据] 文件大小: {} MB", fileSize / 1024.0 / 1024.0);

            // 2. 备份当前数据
            String backupDir = backupCurrentData();
            log.info("[接收数据] 已备份到: {}", backupDir);

            // 3. 解压覆盖
            unpackIrisData(tempFile);

            // 4. 清理临时文件
            tempFile.delete();

            // 5. 清理旧备份（保留最近3个）
            cleanupOldBackups(3);

            response.put("success", true);
            response.put("message", "虹膜数据同步完成");
            response.put("size", fileSize);
            response.put("backup", backupDir);

            log.info("[接收数据] 成功");

            return response;

        } catch (Exception e) {
            log.error("[接收数据] 失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 获取同步状态
     */
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isSyncing", isSyncing);
        status.put("lastSyncTime", lastSyncTime);
        status.put("lastSyncResult", lastSyncResult);

        // 获取数据目录信息
        File dataDir = new File(IRIS_DATA_DIR);
        if (dataDir.exists()) {
            status.put("dataDirectory", IRIS_DATA_DIR);
            status.put("dataSize", getDirSize(dataDir));
        }

        return status;
    }

    /**
     * 打包虹膜数据到ZIP文件
     */
    private void packIrisData(File zipFile) throws IOException {
        File dataDir = new File(IRIS_DATA_DIR);

        if (!dataDir.exists()) {
            throw new IOException("虹膜数据目录不存在: " + IRIS_DATA_DIR);
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            packDirectory(dataDir, dataDir, zos);
        }

        log.info("[打包] 完成: {}", zipFile.getAbsolutePath());
    }

    /**
     * 递归打包目录
     */
    private void packDirectory(File rootDir, File currentDir, ZipOutputStream zos) throws IOException {
        File[] files = currentDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String relativePath = rootDir.toPath().relativize(file.toPath()).toString();

            if (file.isDirectory()) {
                // 添加目录条目
                ZipEntry dirEntry = new ZipEntry(relativePath + "/");
                zos.putNextEntry(dirEntry);
                zos.closeEntry();
                // 递归打包子目录
                packDirectory(rootDir, file, zos);
            } else {
                // 添加文件
                ZipEntry fileEntry = new ZipEntry(relativePath);
                zos.putNextEntry(fileEntry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }

                zos.closeEntry();
            }
        }
    }

    /**
     * 解压虹膜数据
     */
    private void unpackIrisData(File zipFile) throws IOException {
        File dataDir = new File(IRIS_DATA_DIR);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File targetFile = new File(dataDir, entry.getName());

                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    // 确保父目录存在
                    targetFile.getParentFile().mkdirs();

                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zis.closeEntry();
            }
        }

        log.info("[解压] 完成: {}", dataDir.getAbsolutePath());
    }

    /**
     * 备份当前数据
     */
    private String backupCurrentData() throws IOException {
        File dataDir = new File(IRIS_DATA_DIR);

        if (!dataDir.exists()) {
            log.warn("[备份] 数据目录不存在，跳过备份");
            return null;
        }

        String backupDirName = "iris_backup_" + System.currentTimeMillis();
        File backupDir = new File(TEMP_DIR, backupDirName);
        backupDir.mkdirs();

        // 复制整个数据目录
        copyDirectory(dataDir, new File(backupDir, "data"));

        log.info("[备份] 完成: {}", backupDir.getAbsolutePath());
        return backupDir.getAbsolutePath();
    }

    /**
     * 复制目录
     */
    private void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }

            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyDirectory(file, new File(target, file.getName()));
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 清理旧备份
     */
    private void cleanupOldBackups(int keepCount) {
        try {
            File tempDir = new File(TEMP_DIR);
            File[] backups = tempDir.listFiles((dir, name) -> name.startsWith("iris_backup_"));

            if (backups == null || backups.length <= keepCount) {
                return;
            }

            // 按修改时间排序
            Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());

            // 删除旧备份
            for (int i = keepCount; i < backups.length; i++) {
                deleteDirectory(backups[i]);
                log.info("[清理备份] 删除: {}", backups[i].getName());
            }

        } catch (Exception e) {
            log.error("[清理备份] 失败", e);
        }
    }

    /**
     * 删除目录
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    /**
     * 获取目录大小
     */
    private long getDirSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getDirSize(file);
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }
}

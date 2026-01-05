package cn.simbok.iris.service;

import cn.simbok.iris.helper.IrisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IrisService {
    
    private static final Logger log = LoggerFactory.getLogger(IrisService.class);
    
    private IrisHelper irisHelper;
    private volatile boolean initialized = false;
    
    @Value("${iris.config.common}")
    private String commonConfigPath;
    
    @Value("${iris.config.device}")
    private String deviceConfigPath;
    
    private byte[] latestPreviewFrame;
    private int previewWidth;
    private int previewHeight;
    
    @PostConstruct
    public void init() {
        irisHelper = new IrisHelper();
        log.info("IrisService initialized");
    }
    
    /**
     * 初始化虹膜设备
     */
    public CompletableFuture<Integer> initDevice() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String commonConfig = readResourceString(commonConfigPath);
                int result = irisHelper.init(commonConfig, null, (event, param) -> {
                    log.info("Init callback - event: {}, param: {}", event, param);
                    if (event == 1) {
                        log.warn("Device unplugged");
                        initialized = false;
                    } else if (event == 2) {
                        log.info("Device plugged");
                    }
                    return 0;
                });
                
                if (result == 0) {
                    String devConfig = readResourceString(deviceConfigPath);
                    int devResult = irisHelper.loadDevParams(devConfig);
                    if (devResult != 0) {
                        log.error("Load device params failed: {}", devResult);
                        return devResult;
                    }
                    
                    // 启动预览
                    irisHelper.setPreview((frame, width, height) -> {
                        latestPreviewFrame = frame;
                        previewWidth = width;
                        previewHeight = height;
                        return 0;
                    });
                    
                    initialized = true;
                    log.info("Device initialized successfully");
                } else {
                    log.error("Device init failed: {} - {}", result, irisHelper.err2str(result));
                }
                
                return result;
            } catch (Exception e) {
                log.error("Init device error", e);
                return -1;
            }
        });
    }
    
    /**
     * 注册用户
     */
    public CompletableFuture<String> enrollUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            
            int result = irisHelper.enroll(userId, true, (uid, res, which, finished) -> {
                log.info("Enroll callback - user: {}, result: {}, eye: {}, finished: {}", 
                        uid, res, which, finished);
                if (finished == 1) {
                    if (res == 0) {
                        future.complete("注册成功: " + uid);
                    } else {
                        future.complete("注册失败: " + irisHelper.err2str(res));
                    }
                }
                return 0;
            });
            
            if (result != 0) {
                return "启动注册失败: " + irisHelper.err2str(result);
            }
            
            try {
                return future.get();
            } catch (Exception e) {
                log.error("Enroll error", e);
                return "注册异常: " + e.getMessage();
            }
        });
    }
    
    /**
     * 识别用户
     */
    public CompletableFuture<String> identifyUser(boolean continuous) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            
            int result = irisHelper.identify(null, continuous, (uid, res, which, finished) -> {
                log.info("Identify callback - user: {}, result: {}, eye: {}, finished: {}", 
                        uid, res, which, finished);
                if (finished == 1) {
                    if (res == 0) {
                        future.complete("识别成功: " + uid);
                    } else {
                        future.complete("识别失败");
                    }
                }
                return 0;
            });
            
            if (result != 0) {
                return "启动识别失败: " + irisHelper.err2str(result);
            }
            
            try {
                return future.get();
            } catch (Exception e) {
                log.error("Identify error", e);
                return "识别异常: " + e.getMessage();
            }
        });
    }
    
    /**
     * 获取用户列表
     */
    public List<String> getUserList() {
        return irisHelper.getUserList();
    }
    
    /**
     * 删除用户
     */
    public int deleteUser(String userId) {
        return irisHelper.deleteUser(userId);
    }
    
    /**
     * 停止当前操作
     */
    public int stop() {
        return irisHelper.stop();
    }
    
    /**
     * 获取最新预览帧
     */
    public byte[] getLatestPreviewFrame() {
        return latestPreviewFrame;
    }
    
    public int getPreviewWidth() {
        return previewWidth;
    }
    
    public int getPreviewHeight() {
        return previewHeight;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 读取资源文件
     */
    private String readResourceString(String path) {
        try {
            InputStream ips = getClass().getClassLoader().getResourceAsStream(path);
            if (ips == null) {
                log.error("Resource not found: {}", path);
                return "";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(ips, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Read resource error: {}", path, e);
            return "";
        }
    }
}


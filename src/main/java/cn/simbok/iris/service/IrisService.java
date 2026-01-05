package cn.simbok.iris.service;

import cn.simbok.irisHelper.IrisHelper;
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
        try {
            irisHelper = new IrisHelper();
            log.info("IrisService initialized (JNI library will load on first use)");
        } catch (Exception e) {
            log.error("Failed to initialize IrisService", e);
            // 不抛出异常，允许服务启动，在实际使用时再报错
        }
    }
    
    /**
     * 初始化虹膜设备（同步方法，避免ForkJoinPool线程问题）
     */
    public Integer initDevice() {
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
                log.info("Setting up preview callback...");
                int previewResult = irisHelper.setPreview((frame, width, height) -> {
                    if (latestPreviewFrame == null) {
                        log.info("First preview frame received: {}x{}, size: {}", width, height, frame != null ? frame.length : 0);
                    }
                    latestPreviewFrame = frame;
                    previewWidth = width;
                    previewHeight = height;
                    return 0;
                });
                log.info("Preview setup result: {} - Callback registered", previewResult);
                
                // 等待第一帧
                Thread.sleep(500);
                if (latestPreviewFrame == null) {
                    log.warn("No preview frame received after 500ms - trying startPurePreview");
                    int purePreviewResult = irisHelper.startPurePreview();
                    log.info("startPurePreview result: {}", purePreviewResult);
                    Thread.sleep(500);
                    
                    if (latestPreviewFrame == null) {
                        log.warn("Still no preview frame after startPurePreview");
                    } else {
                        log.info("Preview is working after startPurePreview - frame size: {}x{}", previewWidth, previewHeight);
                    }
                } else {
                    log.info("Preview is working - frame size: {}x{}", previewWidth, previewHeight);
                }
                
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
     * 删除所有用户
     */
    public int deleteAllUsers() {
        return irisHelper.deleteAllUser();
    }
    
    /**
     * 添加虹膜用户（通过特征数据）
     */
    public int addIrisUser(String userId, byte[] irisLeft, byte[] irisRight) {
        return irisHelper.addIrisUser(userId, irisLeft, irisRight);
    }
    
    /**
     * 获取设备信息
     */
    public String getDeviceInfo() {
        try {
            return irisHelper.getDeviceInfo();
        } catch (IrisHelper.EmptyDeviceInfoException e) {
            log.error("Get device info error", e);
            return null;
        }
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        return irisHelper.getEngineInfo();
    }
    
    /**
     * 获取运行时信息
     */
    public String getRuntimeInfo() {
        return irisHelper.getRuntimeInfo();
    }
    
    /**
     * 获取注册数据
     */
    public String getEnrollData(String userId) {
        return irisHelper.getEnrollData(userId);
    }
    
    /**
     * 获取识别数据
     */
    public String getIdentifyData() {
        return irisHelper.getIdentifyData();
    }
    
    /**
     * 硬件检测
     */
    public CompletableFuture<String> checkHardware() {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            
            int result = irisHelper.checkHardware((name, res, which, finished) -> {
                log.info("Hardware check callback - result: {}, eye: {}, finished: {}", 
                        res, which, finished);
                if (finished == 1) {
                    if (res == 0) {
                        future.complete("硬件检测通过");
                    } else {
                        future.complete("硬件检测失败: " + res);
                    }
                }
                return 0;
            });
            
            if (result != 0) {
                return "启动硬件检测失败: " + irisHelper.err2str(result);
            }
            
            try {
                return future.get();
            } catch (Exception e) {
                log.error("Hardware check error", e);
                return "硬件检测异常: " + e.getMessage();
            }
        });
    }
    
    /**
     * 更新固件
     */
    public int updateFirmware(byte[] firmware) {
        return irisHelper.updateFirmware(firmware);
    }
    
    /**
     * 动态修改配置
     */
    public int changeConfigure(String config) {
        return irisHelper.changeConfigure(config);
    }
    
    /**
     * 启动纯预览模式
     */
    public int startPurePreview() {
        return irisHelper.startPurePreview();
    }
    
    /**
     * 停止纯预览模式
     */
    public int stopPurePreview() {
        return irisHelper.stopPurePreview();
    }
    
    /**
     * 释放资源
     */
    public void release() {
        irisHelper.release();
        initialized = false;
    }
    
    /**
     * 获取错误码描述
     */
    public String getErrorMessage(int errorCode) {
        return irisHelper.err2str(errorCode);
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


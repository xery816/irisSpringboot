package cn.simbok.iris.config;

import cn.simbok.iris.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class IrisConfig {
    
    private static final Logger log = LoggerFactory.getLogger(IrisConfig.class);
    
    @Value("${iris.library.windows.path}")
    private String windowsLibPath;
    
    @Value("${iris.library.linux.path}")
    private String linuxLibPath;
    
    @PostConstruct
    public void init() {
        try {
            // 详细的平台检测信息
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            String osVersion = System.getProperty("os.version");
            log.info("=== Platform Detection ===");
            log.info("OS Name: {}", osName);
            log.info("OS Architecture: {}", osArch);
            log.info("OS Version: {}", osVersion);
            
            String libraryPath;
            String platform;
            
            if (PlatformUtils.isWindows()) {
                libraryPath = windowsLibPath;
                platform = "Windows";
                log.info("Detected platform: Windows");
            } else if (PlatformUtils.isLinux()) {
                libraryPath = linuxLibPath;
                platform = "Linux";
                log.info("Detected platform: Linux");
            } else {
                log.error("Unsupported platform: {}", osName);
                throw new UnsupportedOperationException("Unsupported OS: " + osName);
            }
            
            // 转换相对路径为绝对路径（基于当前工作目录）
            libraryPath = resolveLibraryPath(libraryPath);
            
            // 设置JNI库路径
            System.setProperty("java.library.path", libraryPath);
            log.info("=== Library Configuration ===");
            log.info("Current working directory: {}", System.getProperty("user.dir"));
            log.info("Native library path: {}", libraryPath);
            log.info("Platform-specific config: iris.library.{}.path", platform.toLowerCase());
            log.info("Note: Native libraries will be loaded on first API call (lazy loading)");
            
            // 强制重新加载库路径
            try {
                java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);
                log.info("Library path reloaded successfully");
            } catch (Exception e) {
                log.warn("Failed to reset sys_paths", e);
            }
        } catch (Exception e) {
            log.error("IrisConfig initialization failed, but application will continue", e);
        }
    }
    
    /**
     * 解析库路径，支持相对路径和绝对路径
     */
    private String resolveLibraryPath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        
        // 如果是绝对路径，直接返回
        if (new java.io.File(path).isAbsolute()) {
            return path;
        }
        
        // 相对路径转换为绝对路径（相对于项目根目录）
        String userDir = System.getProperty("user.dir");
        String absolutePath = new java.io.File(userDir, path).getAbsolutePath();
        log.debug("Resolved relative path '{}' to '{}'", path, absolutePath);
        return absolutePath;
    }
}


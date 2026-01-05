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
        String libraryPath;
        
        if (PlatformUtils.isWindows()) {
            libraryPath = windowsLibPath;
            log.info("Detected Windows platform");
        } else if (PlatformUtils.isLinux()) {
            libraryPath = linuxLibPath;
            log.info("Detected Linux platform");
        } else {
            log.error("Unsupported platform: {}", PlatformUtils.getOsName());
            throw new UnsupportedOperationException("Unsupported OS");
        }
        
        // 设置JNI库路径
        System.setProperty("java.library.path", libraryPath);
        log.info("Set java.library.path to: {}", libraryPath);
        
        // 强制重新加载库路径
        try {
            java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            log.warn("Failed to reset sys_paths", e);
        }
    }
}


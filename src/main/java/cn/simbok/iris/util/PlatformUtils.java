package cn.simbok.iris.util;

public class PlatformUtils {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }
    
    public static boolean isLinux() {
        return OS_NAME.contains("nux") || OS_NAME.contains("nix");
    }
    
    public static String getLibraryName() {
        if (isWindows()) {
            return "irisenginehelper";
        } else if (isLinux()) {
            return "irisenginehelper";
        }
        throw new UnsupportedOperationException("Unsupported OS: " + OS_NAME);
    }
    
    public static String getOsName() {
        return OS_NAME;
    }
}


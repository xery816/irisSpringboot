package cn.simbok.iris.model;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceInfo;
    private String engineInfo;
    private String runtimeInfo;
    private boolean initialized;
}


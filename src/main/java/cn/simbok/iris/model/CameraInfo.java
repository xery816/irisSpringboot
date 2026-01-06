package cn.simbok.iris.model;

import lombok.Data;

@Data
public class CameraInfo {
    private Integer index;
    private String device;
    private Integer width;
    private Integer height;
    private Double fps;
    private String name;
    
    public CameraInfo(Integer index, String device, Integer width, Integer height, Double fps, String name) {
        this.index = index;
        this.device = device;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.name = name;
    }
}


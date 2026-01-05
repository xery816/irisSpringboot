package cn.simbok.iris.model;

import lombok.Data;

@Data
public class IdentifyResult {
    private String userId;
    private boolean success;
    private String message;
    private String identifyData;
    private int whichEye;
}


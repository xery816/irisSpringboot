package cn.simbok.iris.model;

import lombok.Data;

@Data
public class EnrollResult {
    private String userId;
    private boolean success;
    private String message;
    private String enrollData;
    private int whichEye;
}


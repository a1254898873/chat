package com.nifengi.www.bo;

/**
 * @author Yu
 * @title: UserBO
 * @projectName netty
 * @date 2022/7/10 11:49
 */
public class UserBO {
    private  String userId;
    private  String faceData;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }
}

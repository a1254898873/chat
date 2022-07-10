package com.nifengi.www.vo;

/**
 * @author Yu
 * @title: FaceVO
 * @projectName netty
 * @date 2022/7/10 18:34
 */
public class FaceVO {
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

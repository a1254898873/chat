package com.nifengi.www.vo;

import lombok.Data;

/**
 * @author Yu
 * @title: UserVO
 * @projectName netty
 * @date 2022/6/25 18:01
 */
@Data
public class UserVO {
    private String id;

    private String username;

    private String faceImage;

    private String faceImageBig;

    private String nickname;

    private String qrcode;

    private String token;

}

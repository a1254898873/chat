package com.nifengi.www.VO;

import lombok.Data;

/**
 * @author Yu
 * @title: UserVO
 * @projectName netty
 * @date 2022/6/25 18:01
 */
@Data
public class UserVo {
    private String id;

    private String username;

    private String faceImage;

    private String faceImageBig;

    private String nickname;

    private String qrcode;
}

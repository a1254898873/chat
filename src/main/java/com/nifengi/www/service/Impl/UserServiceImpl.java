package com.nifengi.www.service.Impl;

import com.nifengi.www.mapper.UserMapper;
import com.nifengi.www.pojo.User;
import com.nifengi.www.service.UserService;
import com.nifengi.www.util.MinioUtils;
import com.nifengi.www.util.QRCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Yu
 * @title: UserServiceImpl
 * @projectName netty
 * @date 2022/6/24 11:11
 */

@Service
public class UserServiceImpl implements UserService {

    //注入mapper
    @Autowired
    UserMapper userMapper;

    @Autowired
    QRCodeUtils qrCodeUtils;

    @Autowired
    MinioUtils minioUtils;



    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User queryUserNameIsExit(String username) {
        User user = userMapper.queryUserNameIsExit(username);
        return user;
    }

    @Override
    public User insert(User user) throws Exception {
        String userId = UUID.randomUUID().toString().replace("-", "");
        //创建二维码对象信息
        BufferedImage image = qrCodeUtils.createQRCode("bird_qrcode:"+user.getUsername());
        //将newImage写入字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( image, "jpg", baos );
        //转换为MultipartFile
        MultipartFile qrcodeFile = new MockMultipartFile(userId + ".jpg", baos.toByteArray());
        String qrCodeURL ="";
        minioUtils.putObject(qrcodeFile.getInputStream(), userId + ".jpg", "image/jpg");
        qrCodeURL = minioUtils.getPath() + userId + ".jpg";
        user.setId(userId);
        user.setQrcode(qrCodeURL);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User result = userMapper.selectByPrimaryKey(user.getId());
        return result;
    }
}

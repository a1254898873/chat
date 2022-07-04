package com.nifengi.www.service;

import com.nifengi.www.pojo.User;

import java.io.IOException;

/**
 * @author Yu
 * @title: UserService
 * @projectName netty
 * @date 2022/6/24 10:55
 */
public interface UserService {

    User getUserById(String id);
    //根据用户名查找指定用户对象
    User queryUserNameIsExit(String username);

    //保存
    User insert(User user) throws Exception;

    //修改用户
    User updateUserInfo(User user);

}

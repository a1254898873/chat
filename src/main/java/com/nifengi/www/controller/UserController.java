package com.nifengi.www.controller;

import com.nifengi.www.VO.UserVo;
import com.nifengi.www.pojo.User;
import com.nifengi.www.service.UserService;
import com.nifengi.www.util.JSONResult;
import com.nifengi.www.util.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * @author Yu
 * @title: UserController
 * @projectName netty
 * @date 2022/6/24 11:21
 */

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    @RequestMapping("/registerOrLogin")
    @ResponseBody
    //用户登录与注册一体化方法
    public JSONResult registerOrlogin(User user) throws Exception {
        System.out.println(user);
        User userResult = userService.queryUserNameIsExit(user.getUsername());
        if(userResult!=null){//此用户存在，可登录
            if(!userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))){
                return JSONResult.errorMsg("密码不正确");
            }
        }else{//注册
            user.setNickname(user.getUsername());
            user.setQrcode("");
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setFaceImage("");
            user.setFaceImageBig("");
            userResult = userService.insert(user);
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userResult,userVo);
        return JSONResult.ok(userVo);
    }

}

package com.nifengi.www.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.nifengi.www.util.MinioUtils;
import com.nifengi.www.vo.UserVO;
import com.nifengi.www.pojo.User;
import com.nifengi.www.service.UserService;
import com.nifengi.www.util.JSONResult;
import com.nifengi.www.util.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @Autowired
    MinioUtils minioUtils;



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
            StpUtil.login(user.getId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userResult,userVO);
            userVO.setToken(StpUtil.getTokenValue());
            return JSONResult.ok(userVO);

        }else{//注册
            user.setNickname(user.getUsername());
            user.setQrcode("");
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setFaceImage("");
            user.setFaceImageBig("");
            userResult = userService.insert(user);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userResult,userVO);
            return JSONResult.ok(userVO);
        }
    }

//    @RequestMapping("/uploadFaceBase64")
//    @ResponseBody
//    //用户头像上传访问方法
//    public JSONResult uploadFaceBase64(@RequestBody UserBO userBO) throws Exception {
//        //获取前端传过来的base64的字符串，然后转为文件对象在进行上传
//        String base64Data = userBO.getFaceData();
//        String userFacePath = "/usr/local/face/"+userBO.getUserId()+"userFaceBase64.png";
//        //调用FileUtils 类中的方法将base64 字符串转为文件对象
//        FileUtils.base64ToFile(userFacePath, base64Data);
//        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
//        //获取fastDFS上传图片的路径
//        String url = fastDFSClient.uploadBase64(multipartFile);
//        System.out.println(url);
//        String thump = "_150x150.";
//        String[] arr = url.split("\\.");
//        String thumpImgUrl = arr[0]+thump+arr[1];
////        String bigFace = "dssdklsdjsdj3498458.png";
////        String thumpFace = "dssdklsdjsdj3498458_150x150.png";
//        //更新用户头像
//        User user = new User();
//        user.setId(userBO.getUserId());
//        user.setFaceImage(thumpImgUrl);
//        user.setFaceImageBig(url);
//        User result = userService.updateUserInfo(user);
//        return  JSONResult.ok(result);
//    }

}

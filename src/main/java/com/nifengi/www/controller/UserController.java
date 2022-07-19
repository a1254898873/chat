package com.nifengi.www.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.nifengi.www.enums.OperatorFriendRequestTypeEnum;
import com.nifengi.www.enums.SearchFriendsStatusEnum;
import com.nifengi.www.pojo.ChatMsg;
import com.nifengi.www.pojo.FriendsRequest;
import com.nifengi.www.util.MinioUtils;
import com.nifengi.www.vo.FriendsRequestVO;
import com.nifengi.www.vo.MyFriendsVO;
import com.nifengi.www.vo.UserVO;
import com.nifengi.www.pojo.User;
import com.nifengi.www.service.UserService;
import com.nifengi.www.util.JSONResult;
import com.nifengi.www.util.MD5Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
            StpUtil.login(userResult.getId());
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

    @RequestMapping("/uploadFace")
    @ResponseBody
    //用户头像上传访问方法
    public JSONResult uploadFaceBase64(MultipartFile file) throws Exception {
        // 为了避免文件名重复，使用UUID重命名文件，将横杠去掉
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        // 上传
        minioUtils.putObject(file.getInputStream(), fileName, file.getContentType());
        String url = minioUtils.getPath() + fileName;
        //更新用户头像
        User user = new User();
        user.setId(StpUtil.getLoginIdAsString());
        user.setFaceImage(url);
        User result = userService.updateUserInfo(user);
        return  JSONResult.ok(result);
    }

    //修改昵称方法
    @RequestMapping("/setNickname")
    @ResponseBody
    public JSONResult setNickName(User user){
        User userResult = userService.updateUserInfo(user);
        return JSONResult.ok(userResult);
    }


    //搜索好友的请求方法
    @RequestMapping("/searchFriend")
    @ResponseBody
    public JSONResult searchFriend(String myUserId,String friendUserName){
        /**
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friendUserName);
        if(status==SearchFriendsStatusEnum.SUCCESS.status){
            User user = userService.queryUserNameIsExit(friendUserName);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user,userVO);
            return JSONResult.ok(userVO);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(msg);
        }
    }

    //发送添加好友请求的方法
    @RequestMapping("/addFriendRequest")
    @ResponseBody
    public JSONResult addFriendRequest(String myUserId,String friendUserName){
        if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUserName)){
            return JSONResult.errorMsg("好友信息为空");
        }

        /**
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friendUserName);
        if(status==SearchFriendsStatusEnum.SUCCESS.status){
            userService.sendFriendRequest(myUserId,friendUserName);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(msg);
        }
        return JSONResult.ok();
    }


    //好友请求列表查询
    @RequestMapping("/queryFriendRequest")
    @ResponseBody
    public JSONResult queryFriendRequest(String userId){
        List<FriendsRequestVO> friendRequestList = userService.queryFriendRequestList(userId);
        return JSONResult.ok(friendRequestList);
    }

    //好友请求处理映射my_friends
    @RequestMapping("/operFriendRequest")
    @ResponseBody
    public JSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        if(operType== OperatorFriendRequestTypeEnum.IGNORE.type){
            //满足此条件将需要对好友请求表中的数据进行删除操作
            userService.deleteFriendRequest(friendsRequest);
        }else if(operType==OperatorFriendRequestTypeEnum.PASS.type){
            //满足此条件表示需要向好友表中添加一条记录，同时删除好友请求表中对应的记录
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        //查询好友表中的列表数据
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return JSONResult.ok(myFriends);
    }

    /**
     * 好友列表查询
     * @param userId
     * @return
     */
    @RequestMapping("/myFriends")
    @ResponseBody
    public JSONResult myFriends(String userId){
        if (StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("用户id为空");
        }
        //数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
        return JSONResult.ok(myFriends);
    }


    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user",user);
        return "user_list";
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    @RequestMapping("/getUnReadMsgList")
    @ResponseBody
    public JSONResult getUnReadMsgList(String acceptUserId){
        if(StringUtils.isBlank(acceptUserId)){
            return JSONResult.errorMsg("接收者ID不能为空");
        }
        //根据接收ID查找为签收的消息列表
        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return JSONResult.ok(unReadMsgList);

    }

}

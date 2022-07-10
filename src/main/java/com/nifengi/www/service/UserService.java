package com.nifengi.www.service;

import com.nifengi.www.vo.FriendsRequestVO;
import com.nifengi.www.vo.MyFriendsVO;
import com.nifengi.www.netty.ChatMsg;
import com.nifengi.www.pojo.FriendsRequest;
import com.nifengi.www.pojo.User;

import java.util.List;

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

    //搜索好友的前置条件接口
    Integer preconditionSearchFriends(String myUserId, String friendUserName);

    //发送好友请求
    void  sendFriendRequest(String myUserId,String friendUserName);

    //好友请求列表查询
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserId);

    //处理好友请求——忽略好友请求
    void deleteFriendRequest(FriendsRequest friendsRequest);

    //处理好友请求——通过好友请求
    void passFriendRequest(String sendUserId,String acceptUserId);

    //好友列表查询
    List<MyFriendsVO> queryMyFriends(String userId);

    //保存用户聊天消息
    String saveMsg(ChatMsg chatMsg);

    void updateMsgSigned(List<String> msgIdList);

    //获取未签收的消息列表
    List<com.nifengi.www.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);

}

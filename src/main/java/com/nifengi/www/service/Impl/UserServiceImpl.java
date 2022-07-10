package com.nifengi.www.service.Impl;

import com.nifengi.www.vo.FriendsRequestVO;
import com.nifengi.www.vo.MyFriendsVO;
import com.nifengi.www.enums.MsgActionEnum;
import com.nifengi.www.enums.MsgSignFlagEnum;
import com.nifengi.www.enums.SearchFriendsStatusEnum;
import com.nifengi.www.mapper.*;
import com.nifengi.www.netty.DataContent;
import com.nifengi.www.netty.UserChanelRel;
import com.nifengi.www.netty.ChatMsg;
import com.nifengi.www.pojo.FriendsRequest;
import com.nifengi.www.pojo.MyFriends;
import com.nifengi.www.pojo.User;
import com.nifengi.www.service.UserService;
import com.nifengi.www.util.JsonUtils;
import com.nifengi.www.util.MinioUtils;
import com.nifengi.www.util.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
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
    MyFriendsMapper myFriendsMapper;

    @Autowired
    FriendsRequestMapper friendsRequestMapper;

    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Autowired
    UserMapperCustom userMapperCustom;

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
        BufferedImage image = qrCodeUtils.createQRCode("bird_qrcode:" + user.getUsername());
        //将newImage写入字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        //转换为MultipartFile
        MultipartFile qrcodeFile = new MockMultipartFile(userId + ".jpg", baos.toByteArray());
        String qrCodeURL = "";
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

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        //1.搜索的用户如果不存在，则返回【无此用户】
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2.搜索的账号如果是你自己，则返回【不能添加自己】
        if (myUserId.equals(user.getId())) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
        MyFriends myfriend = new MyFriends();
        myfriend.setMyUserId(myUserId);
        myfriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myfriend);
        if (myF != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        MyFriends myfriend = new MyFriends();
        myfriend.setMyUserId(myUserId);
        myfriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myfriend);
        if(myF==null){
            FriendsRequest friendsRequest = new FriendsRequest();
            String requestId = UUID.randomUUID().toString().replace("-", "");
            friendsRequest.setId(requestId);
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(user.getId());
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }
    }

    @Override
    public List<FriendsRequestVO> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //进行双向好友数据保存
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);

        //删除好友请求表中的数据
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);

        Channel sendChannel  = UserChanelRel.get(sendUserId);
        if(sendChannel!=null){
            //使用websocket 主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            //消息的推送
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    //通过好友请求并保存数据到my_friends 表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        String recordId = UUID.randomUUID().toString().replace("-", "");

        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return userMapperCustom.queryMyFriends(userId);
    }



    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.nifengi.www.pojo.ChatMsg msgDB = new com.nifengi.www.pojo.ChatMsg();
        String msgId = UUID.randomUUID().toString().replace("-", "");
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);

        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public List<com.nifengi.www.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        List<com.nifengi.www.pojo.ChatMsg> result = chatMsgMapper.getUnReadMsgListByAcceptUid(acceptUserId);
        return result;
    }
}

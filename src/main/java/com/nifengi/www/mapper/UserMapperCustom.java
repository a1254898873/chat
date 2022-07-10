package com.nifengi.www.mapper;

import com.nifengi.www.vo.FriendsRequestVO;
import com.nifengi.www.vo.MyFriendsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Yu
 * @title: UserMapperCustom
 * @projectName netty
 * @date 2022/7/5 16:16
 */
@Mapper
public interface UserMapperCustom {

    List<FriendsRequestVO> queryFriendRequestList(String acceptUserId);
    List<MyFriendsVO> queryMyFriends(String userId);
    void batchUpdateMsgSigned(List<String> msgIdList);

}

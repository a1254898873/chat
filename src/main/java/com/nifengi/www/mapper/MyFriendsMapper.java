package com.nifengi.www.mapper;

import com.nifengi.www.pojo.MyFriends;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriends record);

    int insertSelective(MyFriends record);

    MyFriends selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(MyFriends record);

    int updateByPrimaryKey(MyFriends record);

    MyFriends selectOneByExample(MyFriends mfe);
}
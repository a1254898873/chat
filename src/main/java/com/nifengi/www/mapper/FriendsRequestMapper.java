package com.nifengi.www.mapper;

import com.nifengi.www.pojo.FriendsRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendsRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendsRequest record);

    int insertSelective(FriendsRequest record);

    FriendsRequest selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FriendsRequest record);

    int updateByPrimaryKey(FriendsRequest record);
}
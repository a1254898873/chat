package com.nifengi.www.netty;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yu
 * @title: UserChanelRel
 * @projectName netty
 * @date 2022/7/6 17:04
 * 用户id 和channel 的关联关系处理
 */
public class UserChanelRel {
    private static HashMap<String, Channel> manage = new HashMap<>();

    public static  void put(String senderId,Channel channel){
        manage.put(senderId,channel);
    }

    public static Channel get(String senderId){
        return manage.get(senderId);
    }

    public static void output(){
        for (Map.Entry<String,Channel> entry  :manage.entrySet()) {
            System.out.println("UserId:"+entry.getKey()
                    +",ChannelId:"+entry.getValue().id().asLongText()
            );
        }
    }
}

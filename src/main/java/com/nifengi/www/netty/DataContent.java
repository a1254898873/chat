package com.nifengi.www.netty;



import java.io.Serializable;
import com.nifengi.www.netty.ChatMsg;

/**
 * @author Yu
 * @title: DataContent
 * @projectName netty
 * @date 2022/7/6 15:40
 */
public class DataContent implements Serializable {
    private Integer action;//动作类型
    private ChatMsg chatMsg;//用户的聊天内容
    private String extand;//扩展字段

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }
}

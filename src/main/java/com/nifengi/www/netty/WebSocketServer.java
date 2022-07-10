package com.nifengi.www.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

/**
 * @author Yu
 * @title: WebSocketServer
 * @projectName netty
 * @date 2022/7/6 15:46
 */
@Component
public class WebSocketServer {

    private static class SingletionWSServer {
        static final WebSocketServer instance = new WebSocketServer();
    }

    public static WebSocketServer getInstance() {
        return SingletionWSServer.instance;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    public WebSocketServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());//handler()是发生在初始化的时候，childHandler()是发生在客户端连接之后
    }

    public void start() {
        this.future = server.bind("172.17.9.194",8888);
        if (future.isSuccess()) {
            System.out.println("启动 Netty 成功");
        }
    }
}

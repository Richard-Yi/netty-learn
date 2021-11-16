package com.ricstudio.netty.learn.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

/**
 * @author Richard_yyf
 * @version 1.0 2021/11/16
 */
public class HttpServer {

    public void start(int port) {
        // 配置线程池 主从多线程  Reactor模式
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // 启动器 负责组装串联核心组件
        ServerBootstrap bootstrap = new ServerBootstrap();

        //
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(new InetSocketAddress(port))
                // Channel 初始化
                    // 设置 Channel 类型
                .channel(NioServerSocketChannel.class)
                    // 设置 channel 参数
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 注册 ChannelHandler
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                // http 编解码
                                .addLast("codec", new HttpServerCodec())
                                // httpContent 压缩
                                .addLast("compressor", new HttpContentCompressor())
                                // http 消息聚合
                                .addLast("aggregator", new HttpObjectAggregator(65536))
                                // 自定义逻辑处理类
                                .addLast("handler", new MyHttpServerHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("http Server started, listening on " + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new HttpServer().start(8101);
    }
}

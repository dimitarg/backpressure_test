package com.novarto.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by fmap on 08.11.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        boolean backpressureEnabled = System.getProperty("backpressure")!=null;

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer());

            if(backpressureEnabled)
            {
                b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
            }

            Channel ch = b.bind(8080).sync().channel();

            if(backpressureEnabled)
            {
                System.out.println("Backpressure enabled");
            }
            System.out.println("Open your web browser and navigate to " +
                    "http://127.0.0.1:8080");

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Server shutdown successfully");
        }
    }
}

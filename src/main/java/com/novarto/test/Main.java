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
                    .childHandler(new ServerInitializer(backpressureEnabled));

            if(backpressureEnabled)
            {
//                b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);

                b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                        16 * 1024,
                        32 * 1024
                ));
            }

            int port = getPort();
            Channel ch = b.bind(port).sync().channel();

            if(backpressureEnabled)
            {
                System.out.println("Backpressure enabled");
            }
            System.out.println("Open your web browser and navigate to " +
                    "http://127.0.0.1:"+ port);

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Server shutdown successfully");
        }
    }

    private static int getPort()
    {
        return Integer.parseInt(System.getProperty("port", "8080"));
    }
}

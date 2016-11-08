package com.novarto.test;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.flow.FlowControlHandler;


/**
 * Created by fmap on 07.11.16.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final boolean enableBackPressure;

    public ServerInitializer(boolean enableBackPressure)
    {
        this.enableBackPressure = enableBackPressure;
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024*1000));
        ch.pipeline().addLast(new ServerHandler(enableBackPressure));
    }
}

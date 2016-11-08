package com.novarto.test;

import com.novarto.test.json.OptimizedJacksonEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmap on 08.11.16.
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private final boolean enableBackPressure;

    public ServerHandler(boolean enableBackPressure)
    {
        this.enableBackPressure = enableBackPressure;
    }

    private static final OptimizedJacksonEncoder ENC = new OptimizedJacksonEncoder(PooledByteBufAllocator.DEFAULT);
    private static final FullHttpResponse BAD_REQ = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

    static {
        BAD_REQ.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {


        if(enableBackPressure)
        {
            boolean writable = ctx.channel().isWritable();
            ctx.channel().config().setAutoRead(writable);

            //        String msg = writable ? "writability changed to true. Bytes before unwritable: " + ctx.channel().bytesBeforeUnwritable() :
            //                "writability changed to false, bytes before writable: " + ctx.channel().bytesBeforeWritable();
            //        System.out.println(msg)
        }


;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {


        if(!ctx.channel().config().isAutoRead())
        {
            System.err.println("autoread is false in channelRead0");
        }

        if(!ctx.channel().isWritable())
        {
            System.err.println("channel is not writable in channelRead0");
        }

        QueryStringDecoder d = new QueryStringDecoder(msg.uri());
        List<String> sizeParam = d.parameters().get("size");

        if (sizeParam == null || sizeParam.size() == 0) {
            ctx.writeAndFlush(BAD_REQ.copy());
            return;
        }


        try {
            long size = Long.parseLong(sizeParam.get(0));


            List<Bean> result = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                result.add(new Bean(i, String.valueOf(i)));
            }

            FullHttpResponse resp = ok(result);
            if(!ctx.channel().isWritable())
            {
                System.err.println("before writeFlush: channel is not writable in channelRead0");
            }

            ctx.writeAndFlush(resp,
                    ctx.voidPromise());
            return;
        } catch (NumberFormatException e) {
            ctx.writeAndFlush(BAD_REQ);
            return;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public static FullHttpResponse ok(Object o) {

        ByteBuf payload = ENC.encode(o);
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, payload);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;

    }
}

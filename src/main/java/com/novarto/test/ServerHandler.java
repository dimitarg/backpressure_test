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


    private static final OptimizedJacksonEncoder ENC = new OptimizedJacksonEncoder(PooledByteBufAllocator.DEFAULT);
    private static final FullHttpResponse BAD_REQ = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

    static {
        BAD_REQ.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {


        QueryStringDecoder d = new QueryStringDecoder(msg.uri());
        List<String> sizeParam = d.parameters().get("size");

        if (sizeParam == null || sizeParam.size() == 0) {
            ctx.channel().writeAndFlush(BAD_REQ.copy());
            return;
        }


        try {
            long size = Long.parseLong(sizeParam.get(0));


            List<Bean> result = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                result.add(new Bean(i, String.valueOf(i)));
            }

            ctx.channel().writeAndFlush(ok(result),
                    ctx.voidPromise());
            return;
        } catch (NumberFormatException e) {
            ctx.channel().writeAndFlush(BAD_REQ);
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

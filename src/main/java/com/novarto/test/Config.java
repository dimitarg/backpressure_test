package com.novarto.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novarto.test.json.OptimizedJacksonEncoder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmap on 09.11.16.
 */
public class Config {

    public static final WriteBufferWaterMark WRITE_WATER_MARK = new WriteBufferWaterMark(
            16 * 1024,
            32 * 1024
    );

    public static final boolean BACKPRESSURE_ENABLED = System.getProperty("backpressure")!=null;

    public static final int PORT = getIntProp("port", 8080);

    public static final OptimizedJacksonEncoder ENC = new OptimizedJacksonEncoder(PooledByteBufAllocator.DEFAULT);

    private static int getIntProp(String propName, int defaultV)
    {
        return Integer.parseInt(System.getProperty(propName, String.valueOf(defaultV)));
    }


    public static final byte[] STATIC_RESPONSE;
    static
    {

        List<Bean> xs = new ArrayList<>();

        int size = getIntProp("staticSize", 5000);
        for(long i=0;i<size;i++)
        {
            xs.add(new Bean(i, String.valueOf(i)));
        }


        try {
            STATIC_RESPONSE = ENC.mapper.writeValueAsString(xs).getBytes(StandardCharsets.UTF_8);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

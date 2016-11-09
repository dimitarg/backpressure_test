package com.novarto.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novarto.test.json.OptimizedJacksonEncoder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmap on 09.11.16.
 */
public class Config {

    public static final Logger LOGGER = LoggerFactory.getLogger("com.novarto.test");

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

    public static int STATIC_RESP_SIZE = getIntProp("staticSize", 5000);

    public static final byte[] STATIC_RESPONSE;
    static
    {

        List<Bean> xs = new ArrayList<>();

        for(long i=0;i<STATIC_RESP_SIZE;i++)
        {
            xs.add(new Bean(i, String.valueOf(i)));
        }


        try {
            STATIC_RESPONSE = ENC.mapper.writeValueAsString(xs).getBytes(StandardCharsets.UTF_8);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString()
    {

        return new StringBuilder().append("BACKPRESSURE_ENABLED: ").append(BACKPRESSURE_ENABLED).append(System.lineSeparator())
                .append("PORT: ").append(PORT).append(System.lineSeparator())
                .append("STATIC_RESP_SIZE: ").append(STATIC_RESP_SIZE).append(System.lineSeparator())
                .append("WRITE_WATER_MARK: ").append(WRITE_WATER_MARK).append(System.lineSeparator())
                .toString();
    }
}

package com.novarto.test.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This Jackson implementation of {@link JsonEncoder} uses the afterburner module
 * plus the custom {@link ByteBufArrayBuilder} which allows us to write directly to the reference
 * counted netty's {@link ByteBuf}
 *
 * Created by Borislav Ivanov on 7/7/15.
 */
public class OptimizedJacksonEncoder extends JsonEncoder
{

    public final ByteBufObjectMapper mapper;

    public OptimizedJacksonEncoder(ByteBufAllocator allocator)
    {
        mapper = new ByteBufObjectMapper(allocator);
        mapper.registerModule(new AfterburnerModule());
        mapper.registerModule(new ParameterNamesModule());

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public ByteBuf encode(Object o)
    {
        try
        {
            return mapper.writeValueAsByteBuf(o);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T decode(ByteBuf o, Class<T> c)
    {
        try
        {
            return mapper.readValue(o.toString(StandardCharsets.UTF_8), c);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
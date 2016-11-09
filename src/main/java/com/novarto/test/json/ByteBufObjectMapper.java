package com.novarto.test.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;

/**
 * Object mapper that uses {@link ByteBufArrayBuilder} to allow serialization to netty's {@link ByteBuf} to avoid allocation
 * <p>
 * Created by Borislav Ivanov on 7/6/15.
 */
public class ByteBufObjectMapper extends ObjectMapper
{
    private static final long serialVersionUID = 1;

    private transient ByteBufAllocator allocator;

    public ByteBufObjectMapper(ByteBufAllocator allocator)
    {
        this.allocator = allocator;
    }

    public ByteBuf writeValueAsByteBuf(Object value) throws JsonProcessingException
    {
        ByteBufArrayBuilder bb = new ByteBufArrayBuilder(this._jsonFactory._getBufferRecycler());

        try
        {
            this._configAndWriteValue(this._jsonFactory.createGenerator(bb, JsonEncoding.UTF8), value);
        }
        catch (JsonProcessingException var4)
        {
            throw var4;
        }
        catch (IOException var5)
        {
            throw JsonMappingException.fromUnexpectedIOE(var5);
        }

        ByteBuf result = bb.toByteBuf(allocator);
        bb.release();
        return result;
    }
}

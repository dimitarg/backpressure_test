package com.novarto.test.json;

import io.netty.buffer.ByteBuf;

/**
 * If you create a custom {@link JsonEncoder} please make sure that it is thread safe
 *
 * Created by Borislav Ivanov on 7/7/15.
 */
public abstract class JsonEncoder
{

    public abstract ByteBuf encode(Object o);

    public abstract <T> T decode(ByteBuf o, Class<T> c);

}

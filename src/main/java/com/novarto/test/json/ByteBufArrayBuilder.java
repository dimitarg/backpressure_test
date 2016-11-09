package com.novarto.test.json;

import com.fasterxml.jackson.core.util.BufferRecycler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.OutputStream;
import java.util.LinkedList;

/**
 * This class is exact copy of {@link com.fasterxml.jackson.core.util.ByteArrayBuilder}. The method
 * toByteArray of the original file were re-written to use netty's {@link ByteBuf} to avoid allocation
 * <p>
 * Created by Borislav Ivanov on 7/6/15.
 */
public class ByteBufArrayBuilder extends OutputStream
{

    // Size of the first block we will allocate.
    private static final int INITIAL_BLOCK_SIZE = 500;

    // Maximum block size we will use for individual non-aggregated
    // blocks. Let's limit to using 256k chunks.
    private static final int MAX_BLOCK_SIZE = (1 << 18);

    // Optional buffer recycler instance that we can use for allocating the first block.
    private final BufferRecycler bufferRecycler;
    private final LinkedList<byte[]> pastBlocks = new LinkedList<byte[]>();

    // Number of bytes within byte arrays in {@link _pastBlocks}.
    private int pastLen;
    private byte[] currBlock;
    private int currBlockPtr;

    public ByteBufArrayBuilder(BufferRecycler br)
    {
        this(br, INITIAL_BLOCK_SIZE);
    }

    public ByteBufArrayBuilder(BufferRecycler br, int firstBlockSize)
    {
        bufferRecycler = br;
        currBlock = (br == null) ? new byte[firstBlockSize] : br.allocByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER);
    }

    public void reset()
    {
        pastLen = 0;
        currBlockPtr = 0;

        if (!pastBlocks.isEmpty())
        {
            pastBlocks.clear();
        }
    }

    /**
     * Clean up method to call to release all buffers this object may be
     * using. After calling the method, no other accessors can be used (and
     * attempt to do so may result in an exception)
     */
    public void release()
    {
        reset();
        if (bufferRecycler != null && currBlock != null)
        {
            bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER, currBlock);
            currBlock = null;
        }
    }

    public void append(int i)
    {
        if (currBlockPtr >= currBlock.length)
        {
            allocMore();
        }
        currBlock[currBlockPtr++] = (byte) i;
    }

    /**
     * Method called when results are finalized and we can get the
     * full aggregated result buffer to return to the caller
     */
    public ByteBuf toByteBuf(ByteBufAllocator allocator)
    {
        int totalLen = pastLen + currBlockPtr;

        if (totalLen == 0)
        { // quick check: nothing aggregated?
            return allocator.buffer(0, 0);
        }

        ByteBuf result = allocator.buffer(totalLen, totalLen);

        int offset = 0;

        for (byte[] block : pastBlocks)
        {
            int len = block.length;
            result.writeBytes(block);
            offset += len;
        }
        result.writeBytes(currBlock, 0, currBlockPtr);

        offset += currBlockPtr;
        if (offset != totalLen)
        { // just a sanity check
            throw new RuntimeException("Internal error: total len assumed to be " + totalLen + ", copied " + offset + " bytes");
        }
        // Let's only reset if there's sizable use, otherwise will get reset later on
        if (!pastBlocks.isEmpty())
        {
            reset();
        }
        return result;
    }

    @Override public void write(byte[] b)
    {
        write(b, 0, b.length);
    }

    @Override public void write(byte[] b, int off, int len)
    {
        while (true)
        {
            int max = currBlock.length - currBlockPtr;
            int toCopy = Math.min(max, len);
            if (toCopy > 0)
            {
                System.arraycopy(b, off, currBlock, currBlockPtr, toCopy);
                off += toCopy;
                currBlockPtr += toCopy;
                len -= toCopy;
            }
            if (len <= 0)
            {
                break;
            }
            allocMore();
        }
    }

    @Override public void write(int b)
    {
        append(b);
    }

    @Override public void close()
    { /* NOP */ }

    @Override public void flush()
    { /* NOP */ }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private void allocMore()
    {
        pastLen += currBlock.length;

        /* Let's allocate block that's half the total size, except
         * never smaller than twice the initial block size.
         * The idea is just to grow with reasonable rate, to optimize
         * between minimal number of chunks and minimal amount of
         * wasted space.
         */
        int newSize = Math.max((pastLen >> 1), (INITIAL_BLOCK_SIZE + INITIAL_BLOCK_SIZE));
        // plus not to exceed max we define...
        if (newSize > MAX_BLOCK_SIZE)
        {
            newSize = MAX_BLOCK_SIZE;
        }
        pastBlocks.add(currBlock);
        currBlock = new byte[newSize];
        currBlockPtr = 0;
    }

}

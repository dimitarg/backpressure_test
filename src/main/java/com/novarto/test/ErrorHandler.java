package com.novarto.test;

import com.sun.management.HotSpotDiagnosticMXBean;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.OutOfDirectMemoryError;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by fmap on 08.11.16.
 */
public class ErrorHandler extends ChannelDuplexHandler {
    private static final Object ABNORMAL_EXIT_MONITOR = new Object();
    private static final String HOTSPOT_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";

    private static final HotSpotDiagnosticMXBean HEAP_DUMPER;


    static {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            HEAP_DUMPER = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        exitIfFatal(throwable);

        throwable.printStackTrace();
        ctx.close();

    }

    private void exitIfFatal(Throwable t) {
        if (t instanceof Error && isFatal((Error) t)) {
            synchronized (ABNORMAL_EXIT_MONITOR) {
                try {
                    t.printStackTrace();
                    System.err.println("Fatal error occurred. Terminating VM.");

                    if (t instanceof OutOfDirectMemoryError) {
                        performHeapDump();
                    }
                } finally {
                    System.exit(1);
                }
            }

        }
    }



    boolean isFatal(Error e) {
        return (e instanceof VirtualMachineError || e instanceof LinkageError);
    }

    private void performHeapDump() {
        try {
            String heapPath = getHeapDumpFile().getAbsolutePath();
            System.err.println("dumping heap due to direct OOM. Path: "  + heapPath);
            HEAP_DUMPER.dumpHeap(heapPath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getHeapDumpFile() {
        String pidLike = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@", "_");

        File targetFile = new File(pidLike + ".hprof");
        if (targetFile.exists()) {
            pidLike = pidLike + "_" + System.currentTimeMillis();
            targetFile = new File(pidLike + ".hprof");
        }

        return targetFile;
    }
}

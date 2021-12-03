package com.ricstudio.netty.learn.custom;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.internal.PlatformDependent;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * 参考：https://stackoverflow.com/questions/19312306/how-to-unit-test-netty-handler/34790002
 * The resource leak detection mechanism in Netty relies on ReferenceQueue and PhantomReference. The garbage collector is not very deterministic about when it notifies the ReferenceQueue when an object (ByteBuf in our case) becomes unreachable. If the VM terminates too early or garbage is not collected soon enough, the resource leak detector cannot tell if there was a leak or not, because the garbage collector didn't tell us anything.
 * Practically, this is not a concern, because a Netty application usually runs for a much longer period in reality and you'll get notified eventually. Just run an application for about 30 seconds and let it do some busy job. You should definitely see the leak error message.
 * -Dio.netty.leakDetectionLevel=PARANOID
 *
 * @author Richard_yyf
 * @version 1.0 2021/12/3
 */
public class NettyLeakDetectTest {

    @Test
    public void testRetainLeakDetect() throws InterruptedException {
        EmbeddedChannel channel = new EmbeddedChannel(new StringDecoder(StandardCharsets.UTF_8));
        System.out.println("usedDirectMemory:" + PlatformDependent.usedDirectMemory());
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            new Thread(() -> {
                ByteBuf buffer = Unpooled.directBuffer(512);
                System.out.println("usedDirectMemory:" + PlatformDependent.usedDirectMemory());
                System.out.println("refCnt:" + buffer.refCnt());
                buffer.writeBytes("测试".getBytes(StandardCharsets.UTF_8));
                buffer.retain();
                System.out.println("refCnt:" + buffer.refCnt());
//                channel.writeInbound(buffer);
//                System.out.println("refCnt:" + buffer.refCnt());
//                String msg = channel.readInbound();
//                System.out.println(msg);
                if (finalI % 2 == 0) {
                    System.gc();
                }
            buffer.release();
            }).start();
            Thread.sleep(1000);
        }

//        String myObject = channel.readInbound();
//        assertEquals("测试", myObject);
        Thread.sleep(1000);
        System.out.println("usedDirectMemory:" + PlatformDependent.usedDirectMemory());
    }
}

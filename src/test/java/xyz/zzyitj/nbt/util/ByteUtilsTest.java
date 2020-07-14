package xyz.zzyitj.nbt.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/5/31 11:10 上午
 * @since 1.0
 */
class ByteUtilsTest {

    /**
     * 测试16进制的byte[]转换为10进制int
     */
    @Test
    void bytes2int() {
        byte[] data = {
                0x7F, 0x1C, 0x7F, 0x1C
        };
        // ByteBuf也可以实现，后续可能会替换
        ByteBuf buf = Unpooled.copiedBuffer(data);
        System.out.println(buf.getUnsignedInt(0));
        System.out.println(buf.getUnsignedMedium(1));
        System.out.println(buf.getUnsignedShort(2));
        System.out.println(buf.getUnsignedByte(3));
        byte[] data2 = {
                0, 0, -128, 0
        };
        ByteBuf buf2 = Unpooled.copiedBuffer(data2);
        System.out.println(buf2.getUnsignedInt(0));
    }

    @Test
    void intToBytes() {
        System.out.println(Arrays.toString(ByteUtils.intToBytesBigEndian(237651)));
        System.out.println(Arrays.toString(ByteUtils.intToBytesLittleEndian(237651)));
    }

    @Test
    void getRandBytes() {
        System.out.println(Arrays.toString(ByteUtils.getRandBytes(2)));
        System.out.println(Arrays.toString(ByteUtils.getRandBytes(4)));
    }
}
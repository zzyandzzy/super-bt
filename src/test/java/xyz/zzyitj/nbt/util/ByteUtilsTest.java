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
    void shortToBytesBigEndian() {
        short s1 = RandomUtils.getRandShort();
        System.out.println(s1);
        ByteBuf buf = Unpooled.copiedBuffer(ByteUtils.shortToBytesBigEndian(s1));
        System.out.println(buf.readShort());
        buf.release();
        System.out.println(Arrays.toString(ByteUtils.shortToBytesBigEndian(s1)));
    }

    @Test
    void intToBytesLittleEndian() {
        System.out.println(Arrays.toString(ByteUtils.intToBytesLittleEndian(Integer.MAX_VALUE)));
        ByteBuf buf = Unpooled.copiedBuffer(ByteUtils.intToBytesLittleEndian(Integer.MAX_VALUE));
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        System.out.println(Arrays.toString(bytes));
        buf.release();
    }

    @Test
    void intToBytesBigEndian() {
        System.out.println(Arrays.toString(ByteUtils.intToBytesBigEndian(Integer.MIN_VALUE)));
        ByteBuf buf = Unpooled.copiedBuffer(ByteUtils.intToBytesBigEndian(Integer.MIN_VALUE));
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        System.out.println(Arrays.toString(bytes));
        buf.release();
    }
}
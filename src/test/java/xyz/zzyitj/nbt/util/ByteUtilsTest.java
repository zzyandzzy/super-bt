package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
     * {@link ByteUtils#bytesToInt(byte[], int, int)}
     */
    @Test
    void bytes2int() {
        byte[] data = {
                0x7F, 0x1C, 0x7F, 0x1C
        };
        Assertions.assertEquals(ByteUtils.bytesToInt(data, 0, 3), 2132573980);
        Assertions.assertEquals(ByteUtils.bytesToInt(data, 1, 3), 1867548);
        Assertions.assertEquals(ByteUtils.bytesToInt(data, 2, 3), 32540);
        Assertions.assertEquals(ByteUtils.bytesToInt(data, 3, 3), 28);
        Assertions.assertEquals(ByteUtils.bytesToInt(data, 0, 4), 0);
    }
}
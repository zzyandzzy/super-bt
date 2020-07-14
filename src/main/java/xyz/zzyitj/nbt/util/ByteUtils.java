package xyz.zzyitj.nbt.util;

import java.util.Random;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/5/31 11:08 上午
 * @since 1.0
 */
public class ByteUtils {
    public static final long BYTE_KB = 1024L;
    public static final long BYTE_MB = BYTE_KB * BYTE_KB;
    public static final long BYTE_GB = BYTE_KB * BYTE_MB;
    public static final long BYTE_TB = BYTE_KB * BYTE_GB;
    public static final long BYTE_PB = BYTE_KB * BYTE_TB;

    /**
     * 小端存储：Little-endian
     * 10进制的int转16进制的字节数组
     * 将int转为低字节在前，高字节在后的byte数组
     * 数据的高字节保存在内存的高地址中
     * 数据的低字节保存在内存的低地址中
     *
     * @param val int
     * @return 字节数组
     */
    public static byte[] intToBytesLittleEndian(int val) {
        byte[] data = new byte[4];
        data[0] = (byte) (val & 0xff);
        data[1] = (byte) ((val >> 8) & 0xff);
        data[2] = (byte) ((val >> 16) & 0xff);
        data[3] = (byte) ((val >> 24) & 0xff);
        return data;
    }

    /**
     * 大端存储：Big-endian
     * 10进制的int转16进制的字节数组
     * 数据的高字节保存在内存的低地址中
     * 数据的低字节保存在内存的高地址中
     *
     * @param val int
     * @return byte[]
     */
    public static byte[] intToBytesBigEndian(int val) {
        byte[] data = new byte[4];
        data[3] = (byte) (val & 0xff);
        data[2] = (byte) (val >> 8 & 0xff);
        data[1] = (byte) (val >> 16 & 0xff);
        data[0] = (byte) (val >> 24 & 0xff);
        return data;
    }

    /**
     * 根据字节数返回随机的字节
     *
     * @param len
     * @return
     */
    public static byte[] getRandBytes(int len) {
        byte[] bytes = new byte[len];
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) random.nextInt(255);
        }
        return bytes;
    }
}

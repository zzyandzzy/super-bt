package xyz.zzyitj.nbt.util;

import java.util.Random;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/16 10:38 上午
 * @since 1.0
 */
public class RandomUtils {
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
            // [-128, 127]
            bytes[i] = (byte) ((random.nextInt(Byte.MAX_VALUE * 2 + 1) + Byte.MIN_VALUE));
        }
        return bytes;
    }

    /**
     * 返回short
     *
     * @return short
     */
    public static short getRandShort() {
        Random random = new Random();
        // [-32768, 32767]
        return (short) (random.nextInt(Short.MAX_VALUE * 2 + 1) + Short.MIN_VALUE);
    }

    /**
     * 返回int
     *
     * @return int
     */
    public static int getRandInt() {
        Random random = new Random();
        return random.nextInt();
    }

    /**
     * 返回int
     *
     * @return int
     */
    public static int getRandInt(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }
}

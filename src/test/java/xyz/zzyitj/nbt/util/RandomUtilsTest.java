package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/16 10:44 上午
 * @since 1.0
 */
class RandomUtilsTest {

    @Test
    void getRandBytes() {
        System.out.println(Arrays.toString(RandomUtils.getRandBytes(2)));
        System.out.println(Arrays.toString(RandomUtils.getRandBytes(4)));
    }

    @Test
    void getRandShort() {
        for (int i = 0; i < 10; i++) {
            System.out.println(RandomUtils.getRandShort());
        }
    }
}
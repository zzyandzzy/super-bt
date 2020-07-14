package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 6:59 下午
 * @since 1.0
 */
class UTPHeaderUtilsTest {

    @Test
    void buildInitHeader() {
        System.out.println(Arrays.toString(UTPHeaderUtils.buildInitHeaderBytes()));
    }

    @Test
    void utpHeaderToBytes() {
    }
}
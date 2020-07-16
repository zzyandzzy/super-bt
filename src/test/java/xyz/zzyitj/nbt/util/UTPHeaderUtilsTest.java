package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.UTPHeader;

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
        System.out.println(UTPHeaderUtils.buildInitHeader());
    }

    @Test
    void buildInitHeaderBytes() {
        System.out.println(Arrays.toString(UTPHeaderUtils.buildInitHeaderBytes()));
    }

    @Test
    void utpHeaderToBytes() {
        UTPHeader utpHeader = UTPHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
    }

    @Test
    void bytesToUtpHeader() {
        UTPHeader utpHeader = UTPHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
        System.out.println(UTPHeaderUtils.bytesToUtpHeader(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
    }
}
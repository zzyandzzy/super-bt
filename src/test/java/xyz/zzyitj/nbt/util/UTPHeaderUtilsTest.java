package xyz.zzyitj.nbt.util;

import org.junit.Test;
import xyz.zzyitj.nbt.bean.UTPHeader;

import java.util.Arrays;


/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 6:59 下午
 * @since 1.0
 */
public class UTPHeaderUtilsTest {

    @Test
    public void buildInitHeader() {
        System.out.println(UTPHeaderUtils.buildInitHeader());
    }

    @Test
    public void buildInitHeaderBytes() {
        System.out.println(Arrays.toString(UTPHeaderUtils.buildInitHeaderBytes()));
    }

    @Test
    public void utpHeaderToBytes() {
        UTPHeader utpHeader = UTPHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
    }

    @Test
    public void bytesToUtpHeader() {
        UTPHeader utpHeader = UTPHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
        System.out.println(UTPHeaderUtils.bytesToUtpHeader(UTPHeaderUtils.utpHeaderToBytes(utpHeader)));
    }
}
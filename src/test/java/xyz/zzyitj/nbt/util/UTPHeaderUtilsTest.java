package xyz.zzyitj.nbt.util;

import org.junit.Test;
import xyz.zzyitj.nbt.protocol.utp.entity.UtpHeader;
import xyz.zzyitj.nbt.protocol.utp.util.UtpHeaderUtils;

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
        System.out.println(UtpHeaderUtils.buildInitHeader());
    }

    @Test
    public void buildInitHeaderBytes() {
        System.out.println(Arrays.toString(UtpHeaderUtils.buildInitHeaderBytes()));
    }

    @Test
    public void utpHeaderToBytes() {
        UtpHeader utpHeader = UtpHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UtpHeaderUtils.utpHeaderToBytes(utpHeader)));
    }

    @Test
    public void bytesToUtpHeader() {
        UtpHeader utpHeader = UtpHeaderUtils.buildInitHeader();
        System.out.println(utpHeader);
        System.out.println(Arrays.toString(UtpHeaderUtils.utpHeaderToBytes(utpHeader)));
        System.out.println(UtpHeaderUtils.bytesToUtpHeader(UtpHeaderUtils.utpHeaderToBytes(utpHeader)));
    }
}
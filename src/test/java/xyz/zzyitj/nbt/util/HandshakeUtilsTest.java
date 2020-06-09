package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.PeerWire;

import java.util.Arrays;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:48 下午
 * @email zzy.main@gmail.com
 */
class HandshakeUtilsTest {

    /**
     * {@link HandshakeUtils#isHandshake(byte[])}
     */
    @Test
    void isHandshake() {
    }

    /**
     * {@link HandshakeUtils#buildHandshake(byte[], byte[])}
     */
    @Test
    void buildHandshake() {
        System.out.println(Arrays.toString(HandshakeUtils.buildHandshake(Const.TEST_PEER_ID, Const.TEST_PEER_ID)));
    }

    /**
     * 测试生成Peer Wire协议的握手消息
     * {@link HandshakeUtils#buildMessage(byte, byte[])}
     */
    @Test
    void buildMessage() {
        // no payload
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.KEEP_ALIVE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.UN_CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.INTERESTED)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.NOT_INTERESTED)));
        // need payload
        byte[] havePayload = new byte[]{
                0, 0, 0, -128
        };
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.HAVE, havePayload)));
    }

    /**
     * 测试data字节数组转换为PeerWire
     * {@link HandshakeUtils#parsePeerWire(byte[], int, int)}
     */
    @Test
    void parsePeerWire() {
        byte[] data = {
                0x0, 0x0, 0x0, 0x56, 0x05, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -2
        };
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        System.out.println(peerWire);
        byte[] data1 = {
                0x0, 0x0, 0x0, 0x2, 0x5, -128, 0x0, 0x0, 0x0, 0x1, 0x2,
        };
        peerWire = HandshakeUtils.parsePeerWire(data1, 0, 2);
        System.out.println(peerWire);
        peerWire = HandshakeUtils.parsePeerWire(data1, 6, 1);
        System.out.println(peerWire);
        byte[] data2 = {
                0x0, 0x0, 0x0, 0xb, 0x7, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x40, 0x0, 0x12, 0x32
        };
        peerWire = HandshakeUtils.parsePeerWire(data2);
        System.out.println(peerWire);
    }

    /**
     * 测试函数
     * {@link HandshakeUtils#requestPieceHandler(int, int, int)}
     */
    @Test
    void requestPieceHandler() {
        System.out.println(Arrays.toString(HandshakeUtils.requestPieceHandler(0, 0, 237651)));
        System.out.println(Arrays.toString(HandshakeUtils.requestPieceHandler(0, 237651, 237651)));
        System.out.println(Arrays.toString(HandshakeUtils.requestPieceHandler(1, 237651, 237651)));
        System.out.println(Arrays.toString(HandshakeUtils.requestPieceHandler(237651, 237651, 237651)));
    }
}
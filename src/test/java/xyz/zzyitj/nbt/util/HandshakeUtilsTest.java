package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.PeerWire;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zzyitj.nbt.util.HandshakeUtils.BIT_TORRENT_PROTOCOL;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:48 下午
 * @email zzy.main@gmail.com
 */
class HandshakeUtilsTest {

    /**
     * 测试下{@link HandshakeUtils#BIT_TORRENT_PROTOCOL}会输出啥
     * 结果：BitTorrent Protocol
     */
    @Test
    void testBTProtocol() {
        System.out.println(new String(BIT_TORRENT_PROTOCOL));
    }

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
        System.out.println(Arrays.toString(HandshakeUtils.buildHandshake(Const.TEST_INFO_HASH, Const.TEST_PEER_ID)));
    }

    /**
     * 测试生成Peer Wire协议的握手消息
     * {@link HandshakeUtils#buildMessage(int, int)} ()}
     */
    @Test
    void buildMessage() {
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.KEEP_ALIVE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.UN_CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.INTERESTED)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.NOT_INTERESTED)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.HAVE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.BIT_FIELD)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.REQUEST)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.PIECE)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.CANCEL)));
        System.out.println(Arrays.toString(HandshakeUtils.buildMessage(HandshakeUtils.PORT)));
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
        PeerWire<byte[]> peerWire = HandshakeUtils.parsePeerWire(data);
        System.out.println(peerWire);
        byte[] data1 = {
                0x0, 0x0, 0x0, 0x2, 0x5, -128, 0x0, 0x0, 0x0, 0x1, 0x2,
        };
        peerWire = HandshakeUtils.parsePeerWire(data1, 0, 2);
        System.out.println(peerWire);
        peerWire = HandshakeUtils.parsePeerWire(data1, 6, 1);
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
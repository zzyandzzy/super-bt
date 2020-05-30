package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;

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
     * {@link HandshakeUtils#generateHandshake(byte[], byte[])}
     */
    @Test
    void generateHandshake() {
        System.out.println(Arrays.toString(HandshakeUtils.generateHandshake(Const.TEST_INFO_HASH, Const.TEST_PEER_ID)));
    }

    /**
     * 测试生成Peer Wire协议的握手消息
     * {@link HandshakeUtils#generateMessage(int, int)} ()}
     */
    @Test
    void generateMessage() {
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.KEEP_ALIVE)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.UN_CHOKE)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.INTERESTED)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.NOT_INTERESTED)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.HAVE)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.BIT_FIELD)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.REQUEST)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.PIECE)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.CANCEL)));
        System.out.println(Arrays.toString(HandshakeUtils.generateMessage(HandshakeUtils.PORT)));
    }
}
package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:48 下午
 * @email zzy.main@gmail.com
 */
class HandshakeUtilsTest {

    /**
     * bt协议头：BitTorrent Protocol的字节码
     */
    private static final byte[] BIT_TORRENT_PROTOCOL = {
            66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108
    };

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
     * {@link HandshakeUtils#generateInterested()}
     */
    @Test
    void generateInterested() {
        System.out.println(Arrays.toString(HandshakeUtils.generateInterested()));
    }

    @Test
    void buildUnChokeHandler() {
        System.out.println(Arrays.toString(HandshakeUtils.buildUnChokeHandler()));
    }
}
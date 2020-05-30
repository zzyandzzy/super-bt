package xyz.zzyitj.nbt.util;

import xyz.zzyitj.nbt.bean.PeerWire;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:12 下午
 * @email zzy.main@gmail.com
 */
public class HandshakeUtils {
    /**
     * BT 握手协议
     * 第0个字节必须为19
     * 第1-19个字节为BitTorrent Protocol的字节码
     * 第20-27个字节为0
     * 第28到47个字节为infoHash
     * 第48到67个字节为peerId，即客服端软件和版本号，比如：Transmission 2.84
     * 0-67总共68个字节
     */
    private static final byte[] HANDSHAKE_PACKAGE = {
            19,
            66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * bt协议头：BitTorrent Protocol的字节码
     */
    private static final byte[] BIT_TORRENT_PROTOCOL = {
            66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108
    };

    /**
     * 判断data字节数组是否是和{@link #BIT_TORRENT_PROTOCOL}一致
     * 判断是否为bt协议
     *
     * @param data 字节数组
     * @return true 是bt协议 false 不是bt协议
     */
    public static boolean isHandshake(byte[] data) {
        if (data.length >= HANDSHAKE_PACKAGE.length) {
            for (int i = 0; i < BIT_TORRENT_PROTOCOL.length; i++) {
                if (BIT_TORRENT_PROTOCOL[i] != data[i + 1]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Generate handshake
     * 生成握手
     *
     * @param infoHash info hash
     * @param peerId   客服端标志
     * @return 握手字节数组
     */
    public static byte[] generateHandshake(byte[] infoHash, byte[] peerId) {
        byte[] bytes = HANDSHAKE_PACKAGE;
        System.arraycopy(infoHash, 0, bytes, 28, 20);
        System.arraycopy(peerId, 0, bytes, 48, 20);
        return bytes;
    }

    /**
     * 生成感兴趣（将要下载）的块
     *
     * @return 即将要下载的块
     */
    public static byte[] generateInterested() {
        return new byte[]{
                0, 0, 0, 1, 2
        };
    }

    public static byte[] buildUnChokeHandler() {
        return new byte[]{
                0, 0, 0, 13, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 61
        };
    }

    public static PeerWire<?> parsePeerWire(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return null;
        }
        int id = bytes.length == 4 ? bytes[2] : bytes.length == 5 ? bytes[3] : 0;
        return null;
    }
}

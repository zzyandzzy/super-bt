package xyz.zzyitj.nbt.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.bean.PeerWirePayload;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:12 下午
 */
public class HandshakeUtils {
    /**
     * BT协议的版本：1.0
     */
    public static final byte BIT_TORRENT_PROTOCOL_VERSION_1_0 = 0x13;

    /**
     * 块最大长度为2^14，即16KB
     * 超过此大小peer可能拒绝
     */
    public static final int PIECE_MAX_LENGTH = 2 << 13;

    /**
     * "BitTorrent protocol"共19个字节
     */
    public static final int BIT_TORRENT_PROTOCOL_LENGTH = 19;

    /**
     * peer第一次回复的Handshake长度，为68，即
     * {@link #BIT_TORRENT_PROTOCOL_VERSION_1_0} 1 byte
     * {@link #BIT_TORRENT_PROTOCOL_LENGTH} 19 byte
     * 8个字节为0的保留位
     * info_hash 20 byte
     * peer_id 20 byte
     */
    public static final int HANDSHAKE_LENGTH = 68;
    /**
     * Peer Wire协议
     * 握手消息的格式是这样的：
     * <pstrlen><pstr><reserved><info_hash><peer_id>
     * ----0------19-----27----------47-------67----
     * 第0个字节: 在BitTorrent协议的v1.0版本, pstrlen = 19。1个字节
     * 第1-19个字节: pstr = "BitTorrent protocol"的字节码共19个字节
     * 第20-27个字节为0，保留位，共8个字节
     * 第28到47个字节为info_hash，即种子info块的hash{@link TorrentUtils#hash(byte[])}，共20个字节
     * 第48到67个字节为peerId，即客服端类型和版本号，比如：Transmission 2.84，共20个字节
     * 更多peerId查看<a href="http://bittorrent.org/beps/bep_0x0,20.html"></a>
     * 0-67总共68个字节
     */
    private static final byte[] HANDSHAKE_PACKAGE = {
            BIT_TORRENT_PROTOCOL_VERSION_1_0,
            0x42, 0x69, 0x74, 0x54, 0x6f, 0x72, 0x72, 0x65, 0x6e, 0x74, 0x20, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x63, 0x6f, 0x6c,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * 判断data字节数组是否是和{@link #HANDSHAKE_PACKAGE}的第1个到第20个字节一致
     * 判断是否为bt协议
     *
     * @param data 字节数组
     * @return true 是bt协议 false 不是bt协议
     */
    public static boolean isHandshake(byte[] data) {
        // 忽略掉第一个字节，第一个字节是bt协议的版本
        for (int i = 1; i <= BIT_TORRENT_PROTOCOL_LENGTH; i++) {
            if (HANDSHAKE_PACKAGE[i] != data[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * build handshake
     * 生成握手
     *
     * @param infoHash info hash
     * @param peerId   客服端标志
     * @return 握手字节数组
     */
    public static byte[] buildHandshake(byte[] infoHash, byte[] peerId) {
        byte[] bytes = HANDSHAKE_PACKAGE;
        System.arraycopy(infoHash, 0, bytes, 28, 20);
        System.arraycopy(peerId, 0, bytes, 48, 20);
        return bytes;
    }

    /**
     * 根据消息类型和消息负载生成消息
     * Peer Wire的消息类型有：
     * keep-alive、PeerWireConst.choke(0)、unchoke(1)、
     * interested(2)、not interested(3)、have(4)、
     * bitfield(5)、request(6)、piece(7)、
     * cancel(8)、port(9)
     *
     * <a href="https://github.com/transmission/transmission/wiki/Peer-Status-Text"></a>
     *
     * @param id      消息类型
     * @param payload 消息负载
     * @return 消息体
     */
    public static byte[] buildMessage(byte id, byte[] payload) {
        byte[] ret = new byte[0];
        if (id == PeerWireConst.KEEP_ALIVE) {
            ret = new byte[]{
                    0, 0, 0, 0
            };
        } else if (id >= PeerWireConst.CHOKE && id <= PeerWireConst.NOT_INTERESTED) {
            // 因为id为0到id为3的消息长度都是一样的
            ret = new byte[]{
                    0, 0, 0, 1, id
            };
        } else if (payload != null) {
            // 4 + 1是因为记录消息的长度的字节占4位，id本身占1位
            ret = new byte[4 + 1 + payload.length];
            byte[] lengthBytes = ByteUtils.intToBytesBigEndian(payload.length + 1);
            System.arraycopy(lengthBytes, 0, ret, 0, 4);
            ret[PeerWireConst.PEER_WIRE_ID_INDEX] = id;
            System.arraycopy(payload, 0, ret, 5, payload.length);
        }
        return ret;
    }

    /**
     * 默认生成没有payload的消息
     *
     * @param id 类型
     * @return 消息体
     */
    public static byte[] buildMessage(byte id) {
        return buildMessage(id, null);
    }

    /**
     * 判断bitFiled长度是否符合规则
     *
     * @param peerWire peerWire
     * @return true符合规则，false 不符合规则
     */
    public static boolean isBitField(PeerWire peerWire) {
        return (peerWire.getSize() - 1) == peerWire.getPayloadAsBytes().length;
    }

    /**
     * 把data转换为PeerWire
     *
     * @param data 字节数组
     * @return PeerWire
     */
    public static PeerWire parsePeerWire(byte[] data) {
        // size 为data的0-3位
        ByteBuf buf = Unpooled.copiedBuffer(data, 0, 4);
        int size = (int) buf.getUnsignedInt(0);
        return parsePeerWire(data, 0, size);
    }

    /**
     * 把data转换为PeerWire
     *
     * @param data  字节数组
     * @param start 开始的位置
     * @param size  payload的大小
     * @return PeerWire
     */
    public static PeerWire parsePeerWire(byte[] data, int start, int size) {
        if (data == null || data.length < (start + PeerWireConst.PEER_WIRE_ID_INDEX)) {
            return null;
        }
        // id 为data的第4位
        byte id = data.length > (start + PeerWireConst.PEER_WIRE_ID_INDEX) ? data[start + PeerWireConst.PEER_WIRE_ID_INDEX] : 0;
        PeerWire peerWire = new PeerWire();
        peerWire.setId(id);
        peerWire.setSize(size);
        if (size > 1) {
            // 根据id判断payload类型
            if (id >= PeerWireConst.REQUEST && id <= PeerWireConst.CANCEL) {
                ByteBuf buf = Unpooled.copiedBuffer(data, start + 5, 8);
                int index = (int) buf.getUnsignedInt(0);
                int begin = (int) buf.getUnsignedInt(4);
                byte[] block = new byte[size - 9];
                System.arraycopy(data, start + 13, block, 0, size - 9);
                peerWire.setPayload(new PeerWirePayload(index, begin, block));
            } else {
                byte[] payload = new byte[size - 1];
                System.arraycopy(data, start + 5, payload, 0, size - 1);
                peerWire.setPayload(payload);
            }
        }
        return peerWire;
    }

    /**
     * 构造请求下载指定块参数
     *
     * @param index  指定从零开始的piece索引。
     * @param begin  指定piece中从零开始的字节偏移。
     * @param length 指定请求的长度。
     * @return 字节数组
     */
    public static byte[] requestPieceHandler(int index, int begin, int length) {
        byte[] data = new byte[]{
                0x0, 0x0, 0x0, 0xd, PeerWireConst.REQUEST,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0
        };
        byte[] indexBytes = ByteUtils.intToBytesBigEndian(index);
        System.arraycopy(indexBytes, 0, data, data.length - 12, 4);
        byte[] beginBytes = ByteUtils.intToBytesBigEndian(begin);
        System.arraycopy(beginBytes, 0, data, data.length - 8, 4);
        byte[] lengthBytes = ByteUtils.intToBytesBigEndian(length);
        System.arraycopy(lengthBytes, 0, data, data.length - 4, 4);
        return data;
    }
}

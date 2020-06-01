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
     * keep-alive消息是一个0字节的消息，将length prefix设置成0。没有message ID和payload。
     * <p>
     * 如果peers在一个固定时间段内没有收到任何报文(keep-alive或其他任何报文)，那么peers应该关掉这个连接，
     * 因此如果在一个给定的时间内没有发出任何命令的话，peers必须发送一个keep-alive报文保持这个连接激活。
     * 通常情况下，这个时间是2分钟。
     */
    public static final int KEEP_ALIVE = -1;
    /**
     * choke报文长度固定，并且没有payload。
     */
    public static final int CHOKE = 0;
    /**
     * unchoke报文长度固定，并且没有payload。
     */
    public static final int UN_CHOKE = 1;
    /**
     * interested报文长度固定，并且没有payload。
     * 感兴趣的
     */
    public static final int INTERESTED = 2;
    /**
     * not interested报文长度固定，并且没有payload。
     * 不感兴趣的
     */
    public static final int NOT_INTERESTED = 3;
    /**
     * have报文长度固定。payload是piece(片)的从零开始的索引，该片已经成功下载并且通过hash校验。
     * <p>
     * 实现者注意：实际上，一些客户端必须严格实现该定义。因为peers不太可能下载他们已经拥有的piece(片)，
     * 一个peer不应该通知另一个peer它拥有一个piece(片)，如果另一个peer拥有这个piece(片)。
     * 最低限度”HAVE suppresion”会使用have报文数量减半，总的来说，大致减少25-35%的HAVE报文。
     * 同时，给一个拥有piece(片)的peer发送HAVE报文是值得的，因为这有助于决定哪个piece是稀缺的。
     * <p>
     * 一个恶意的peer可能向其他的peer广播它们不可能下载的piece(片)。
     * Due to this attempting to model peers using this information is a bad idea.
     */
    public static final int HAVE = 4;
    /**
     * bitfield报文可能仅在握手序列发送之后，其他消息发送之前立即发送。
     * 它是可选的，如果一个客户端没有piece(片)，就不需要发送该报文。
     * <p>
     * bitfield报文长度可变，其中x是bitfield的长度。
     * payload是一个bitfield，该bitfield表示已经成功下载的piece(片)。第一个字节的高位相当于piece索引0。
     * 设置为0的位表示一个没有的piece，设置为1的位表示有效的和可用的piece。末尾的冗余位设置为0。
     * <p>
     * 长度不对的bitfield将被认为是一个错误。
     * 如果客户端接收到长度不对的bitfield或者bitfield有任一冗余位集，它应该丢弃这个连接。
     */
    public static final int BIT_FIELD = 5;
    /**
     * request报文长度固定，用于请求一个块(block)。payload包含如下信息：
     * <p>
     * index: 整数，指定从零开始的piece索引。
     * begin: 整数，指定piece中从零开始的字节偏移。
     * length: 整数，指定请求的长度。
     * 根据官方规范有关主要版本3，“所有当前执行应使用 2^15（32 KB），请求数量大于 2^17（128 KB）时应断开连接。
     * ”在主要版本4中，此反应修改到了 2^14（16 KB），超过该值的用户会强迫拒绝。
     * 注意到块请求小于片断大小（>=2^18 字节），所以为下载一个完整片断需要多次请求。
     * <p>
     * 由于新版本将限制定在 16 KB，尝试使用 32 KB 的块就好比用 4 发子弹来玩俄式轮盘——会遇到困难。
     * 更小的请求会导致更大的系统时间和空间开销，因为要跟踪很多请求。结果应使用所有客户端都允许的 16 KB。
     * <p>
     * 请求块大小的限制执行的选择没有减少一部分清楚。在主要版本 4 中，强制使用 16 KB 的请求，
     * 许多客户端会使用该值，只有一个严格客户端组不会使用。
     * 大多数旧客户端使用 32 KB 请求，不允许明显减少可能用户的批次。
     * 同时 16 KB 是现在部分官方的限制（“部分”是因为官方协议文档没有更新），所以强制使用没有错。
     * 另外，允许更大的请求增大了可能用户的批次，除在非常低的带宽连接（小于 256 kbps）中，
     * 多个块会在一个阻塞周期内完成下载，从而强迫使用旧的限制仅会降低很少的性能。
     * 因此，推荐仅在旧的 128 KB 下才强行限制。
     */
    public static final int REQUEST = 6;
    /**
     * piece报文长度可变，其中x是块的长度。payload包含如下信息：
     * <p>
     * index: 整数，指定从零开始的piece索引。
     * begin: 整数，指定piece中从零开始的字节偏移。
     * block: 数据块，它是由索引指定的piece的子集。
     */
    public static final int PIECE = 7;
    /**
     * cancel报文长度固定，用于取消块请求。playload与request报文的playload相同。一般情况下用于结束下载。
     */
    public static final int CANCEL = 8;
    /**
     * port报文由新版本的Mainline发送，新版本Mainline实现了一个DHT tracker。
     * 该监听端口是peer的DHT节点正在监听的端口。这个peer应该插入本地路由表(如果支持DHT tracker的话)。
     */
    public static final int PORT = 9;

    /**
     * Peer返回的PeerWire消息中
     * PeerWire的 id 为data字节数组的第4位
     */
    public static final int PEER_WIRE_ID_INDEX = 4;

    /**
     * BT协议1.0
     */
    public static final byte BIT_TORRENT_PROTOCOL_VERSION_1_0 = 0x13;

    /**
     * 块最大长度为2^14，即16KB
     * 超过此大小peer可能拒绝
     */
    public static final int PIECE_MAX_LENGTH = 2 << 13;


    /**
     * peer第一次回复的Handshake长度，为68，即
     * {@link #BIT_TORRENT_PROTOCOL_VERSION_1_0} 1 byte
     * {@link #BIT_TORRENT_PROTOCOL} 20 byte
     * info_hash 20 byte
     * peer_id 20 byte
     */
    public static final int HANDSHAKE_LENGTH = 68;
    /**
     * Peer Wire协议
     * 握手消息的格式是这样的：
     * <pstrlen><pstr><reserved><info_hash><peer_id>
     * ----0------19-----27----------47-------67----
     * 第0个字节: 在BitTorrent协议的v1.0版本, pstrlen = 19
     * 第1-19个字节: pstr = "BitTorrent protocol"的字节码
     * 第20-27个字节为0
     * 第28到47个字节为info_hash，即种子info块的hash{@link TorrentUtils#hash(byte[])}
     * 第48到67个字节为peerId，即客服端类型和版本号，比如：Transmission 2.84
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
     * bt协议头：BitTorrent Protocol的字节码
     */
    public static final byte[] BIT_TORRENT_PROTOCOL = {
            0x42, 0x69, 0x74, 0x54, 0x6f, 0x72, 0x72, 0x65, 0x6e, 0x74, 0x20, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x63, 0x6f, 0x6c
    };

    /**
     * 判断data字节数组是否是和{@link #BIT_TORRENT_PROTOCOL}一致
     * 判断是否为bt协议
     *
     * @param data 字节数组
     * @return true 是bt协议 false 不是bt协议
     */
    public static boolean isHandshake(byte[] data) {
        for (int i = 0; i < BIT_TORRENT_PROTOCOL.length; i++) {
            if (BIT_TORRENT_PROTOCOL[i] != data[i]) {
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
     * 生成message id为id，类型为type的消息体。
     * Peer Wire的消息类型有：
     * keep-alive、choke(0)、unchoke(1)、
     * interested(2)、not interested(3)、have(4)、
     * bitfield(5)、request(6)、piece(7)、
     * cancel(8)、port(9)
     *
     * <a href="https://github.com/transmission/transmission/wiki/Peer-Status-Text"></a>
     *
     * @param id   message id，第3位
     * @param type message type，第4位
     * @return 消息体
     */
    public static byte[] buildMessage(int id, int type) {
        if (type == -1) {
            id = 0;
            type = 0;
        }
        return new byte[]{
                0, 0, 0, (byte) id, (byte) type
        };
    }

    /**
     * 默认生成id为1的消息体
     *
     * @param type 类型
     * @return 消息体
     */
    public static byte[] buildMessage(int type) {
        return buildMessage(1, type);
    }

    /**
     * 判断bitFiled长度是否符合规则
     *
     * @param peerWire peerWire
     * @return true符合规则，false 不符合规则
     */
    public static boolean isBitField(PeerWire<byte[]> peerWire) {
        return (peerWire.getSize() - 1) == peerWire.getPayload().length;
    }

    /**
     * 把data转换为PeerWire
     *
     * @param data 字节数组
     * @return PeerWire
     */
    public static <T> PeerWire<T> parsePeerWire(byte[] data) {
        // size 为data的0-3位
        int size = ByteUtils.bytesToInt(data, 0, 3);
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
    public static <T> PeerWire<T> parsePeerWire(byte[] data, int start, int size) {
        if (data == null || data.length < (start + PEER_WIRE_ID_INDEX)) {
            return null;
        }
        // id 为data的第4位
        byte id = data.length > (start + PEER_WIRE_ID_INDEX) ? data[start + PEER_WIRE_ID_INDEX] : 0;
        PeerWire<T> peerWire = new PeerWire<>();
        peerWire.setId(id);
        peerWire.setSize(size);
        if (size > 1) {
            // 根据id判断payload类型
            byte[] payload = new byte[size - 1];
            System.arraycopy(data, start + 5, payload, 0, size - 1);
            peerWire.setPayload(payload);
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
                0x0, 0x0, 0x0, 0xd, REQUEST,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0
        };
        byte[] indexData = ByteUtils.intToBytesLittleEndian(index);
        System.arraycopy(indexData, 0, data, data.length - 12, 4);
        byte[] beginData = ByteUtils.intToBytesLittleEndian(begin);
        System.arraycopy(beginData, 0, data, data.length - 8, 4);
        byte[] lengthData = ByteUtils.intToBytesLittleEndian(length);
        System.arraycopy(lengthData, 0, data, data.length - 4, 4);
        return data;
    }
}

package xyz.zzyitj.nbt.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.bean.PeerWirePayload;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:12 下午
 * @email zzy.main@gmail.com
 */
public class HandshakeUtils {
    /**
     * keep_alive消息的长度固定，为4字节，它没有消息编号和负载。
     * 如果一段时间内客户端与peer没有交换任何消息，则与这个peer的连接将被关闭。
     * keep_alive消息用于维持这个连接，通常如果2分钟内没有向peer发送任何消息，
     * 则发送一个keep_alive消息。
     */
    public static final byte KEEP_ALIVE = -1;
    /**
     * choke消息的长度固定，为5字节，消息长度占4个字节，消息编号占1个字节，没有负载。
     * 该消息的功能是，发出该消息的peer将接收该消息的peer阻塞，暂时不允许其下载自己的数据。
     *
     * <a href="http://www.bittorrent.org/beps/bep_0003.html"></a>
     */
    public static final byte CHOKE = 0x0;
    /**
     * unchoke消息的长度固定，为5字节，消息长度占4个字节，消息编号占1个字节，没有负载。
     * 客户端每隔一定的时间，通常为10秒，计算一次各个peer的下载速度，如果某peer被解除阻塞，
     * 则发送unchoke消息。如果某个peer原先是解除阻塞的，而此次被阻塞，则发送choke消息。
     */
    public static final byte UN_CHOKE = 0x1;
    /**
     * interested消息的长度固定，为5字节，消息长度占4个字节，消息编号占1个字节，没有负载。
     * 当客户端收到某peer的have消息时，如果发现peer拥有了客户端没有的piece，
     * 则发送interested消息告知该peer，客户端对它感兴趣。
     */
    public static final byte INTERESTED = 0x2;
    /**
     * not interested消息的长度固定，为5字节，消息长度占4个字节，消息编号占1个字节，没有负载。
     * 当客户端下载了某个piece，如果发现客户端拥有了这个piece后，
     * 某个peer拥有的所有piece，客户端都拥有，则发送not interested消息给该peer。
     */
    public static final byte NOT_INTERESTED = 0x3;
    /**
     * have消息的长度固定，为9字节，消息长度占4个字节，消息编号占1个字节，负载为4个字节。
     * 负载为一个整数，指明下标为index的piece，peer已经拥有。
     * 每当客户端下载了一个piece，即将该piece的下标作为have消息的负载构造have消息，
     * 并把该消息发送给所有与客户端建立连接的peer。
     */
    public static final byte HAVE = 0x4;
    /**
     * bitfield消息的长度不固定，其中X是bitfield(即位图)的长度。
     * 当客户端与peer交换握手消息之后，就交换位图。
     * 位图中，每个piece占一位，若该位的值为1，则表明已经拥有该piece；为0则表明该piece尚未下载。
     * 具体而言，假定某共享文件共拥有801个piece，则位图为101个字节，
     * 位图的第一个字节的最高位指明第一个piece是否拥有，位图的第一个字节的第二高位指明第二个piece是否拥有，依此类推。
     * 对于第801个piece，需要单独一个字节，该字节的最高位指明第801个piece是否已被下载，其余的7位放弃不予使用。
     */
    public static final byte BIT_FIELD = 0x5;
    /**
     * request消息的长度固定，为17个字节，
     * index是piece的索引，begin是piece内的偏移，length是请求peer发送的数据的长度。
     * 当客户端收到某个peer发来的unchoke消息后，即构造request消息，向该peer发送数据请求。
     * 前面提到，peer之间交换数据是以slice（长度为16KB的块）为单位的，因此request消息中length的值一般为16K。
     * 对于一个256KB的piece，客户端分16次下载，每次下载一个16K的slice。
     */
    public static final byte REQUEST = 0x6;
    /**
     * piece消息是另外一个长度不固定的消息，长度前缀中的9是id、index、begin的长度总和，index和begin固定为4字节，
     * X为block的长度，一般为16K。因此对于piece消息，长度前缀加上id通常为00 00 40 09 07。
     * 当客户端收到某个peer的request消息后，如果判定当前未将该peer阻塞，且peer请求的slice，客户端已经下载，
     * 则发送piece消息将文件数据上传给该peer。
     * index: 整数，指定从零开始的piece索引。
     * begin: 整数，指定piece中从零开始的字节偏移。
     * block: 数据块，它是由索引指定的piece的子集。
     */
    public static final byte PIECE = 0x7;
    /**
     * cancel消息的长度固定，为17个字节，len、index、begin、length都占4字节。
     * 它与request消息对应，作用刚好相反，用于取消对某个slice的数据请求。
     * 如果客户端发现，某个piece中的slice，客户端已经下载，而客户端又向其他peer发送了对该slice的请求，
     * 则向该peer发送cancel消息，以取消对该slice的请求。
     * 事实上，如果算法设计合理，基本不用发送cancel消息，只在某些特殊的情况下才需要发送cancel消息。
     */
    public static final byte CANCEL = 0x8;
    /**
     * port消息的长度固定，为7字节，其中listen-port占两个字节。
     * 该消息只在支持DHT的客户端中才会使用，用于指明DHT监听的端口号，一般不必理会，收到该消息时，直接丢弃即可。
     */
    public static final byte PORT = 0x9;
    /**
     * 该消息与任何其他bittorrent消息一样发送，带有4字节长的前缀和一个标识该消息的单字节（在这种情况下，单字节为0x14）。
     * 在消息有效负载的开头，是一个单字节消息标识符。
     * 该标识符可以引用不同的扩展消息，并且仅指定一个ID，即0。
     * 如果ID为0，则该消息为握手消息，如下所述。
     * 通用扩展消息的布局如下（包括bittorrent协议使用的消息头）：
     * <p>
     * 尺寸	    描述
     * uint32_t	长度前缀。指定整个消息的字节数。（大端）
     * uint8_t	bittorrent消息ID，等于0x14
     * uint8_t	扩展消息ID。0为握手，大于0为握手指定的扩展消息。
     *
     * <a href="https://www.bittorrent.org/beps/bep_0010.html"></a>
     */
    public static final byte EXTENDED = 0x14;


    /**
     * Peer返回的PeerWire消息中
     * PeerWire的 id 为data字节数组的第4位
     */
    public static final int PEER_WIRE_ID_INDEX = 4;

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
     * keep-alive、choke(0)、unchoke(1)、
     * interested(2)、not interested(3)、have(4)、
     * bitfield(5)、request(6)、piece(7)、
     * cancel(8)、port(9)
     *
     * <a href="https://github.com/transmission/transmission/wiki/Peer-Status-Text"></a>
     *
     * @param type    消息类型
     * @param payload 消息负载
     * @return 消息体
     */
    public static byte[] buildMessage(byte type, byte[] payload) {
        byte[] ret;
        if (type == KEEP_ALIVE) {
            ret = new byte[]{
                    0, 0, 0, 0
            };
        } else if (type >= CHOKE && type <= NOT_INTERESTED) {
            ret = new byte[]{
                    0, 0, 0, 1, type
            };
        } else {
            ret = new byte[4 + 1 + payload.length];
            byte[] lengthData = ByteUtils.intToBytesBigEndian(payload.length + 1);
            System.arraycopy(lengthData, 0, ret, 0, 4);
            ret[4] = type;
            System.arraycopy(payload, 0, ret, 5, payload.length);
        }
        return ret;
    }

    /**
     * 默认生成id为1的消息体
     *
     * @param type 类型
     * @return 消息体
     */
    public static byte[] buildMessage(byte type) {
        return buildMessage(type, null);
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
        if (data == null || data.length < (start + PEER_WIRE_ID_INDEX)) {
            return null;
        }
        // id 为data的第4位
        byte id = data.length > (start + PEER_WIRE_ID_INDEX) ? data[start + PEER_WIRE_ID_INDEX] : 0;
        PeerWire peerWire = new PeerWire();
        peerWire.setId(id);
        peerWire.setSize(size);
        if (size > 1) {
            // 根据id判断payload类型
            if (id >= HandshakeUtils.REQUEST && id <= HandshakeUtils.CANCEL) {
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
                0x0, 0x0, 0x0, 0xd, REQUEST,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0
        };
        byte[] indexData = ByteUtils.intToBytesBigEndian(index);
        System.arraycopy(indexData, 0, data, data.length - 12, 4);
        byte[] beginData = ByteUtils.intToBytesBigEndian(begin);
        System.arraycopy(beginData, 0, data, data.length - 8, 4);
        byte[] lengthData = ByteUtils.intToBytesBigEndian(length);
        System.arraycopy(lengthData, 0, data, data.length - 4, 4);
        return data;
    }
}

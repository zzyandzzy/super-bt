package xyz.zzyitj.nbt.util;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 6:39 下午
 * @since 1.0
 */
public class PeerWireConst {
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
     * 包头长度字节大小
     */
    public static final int PACKAGE_HEADER_LENGTH = 4;
    /**
     * 最小包大小
     */
    public static final int FRAME_MIN_LENGTH = 4;
    /**
     * 这里帧最大长度加13是因为当帧id为7时
     * 帧长度为HandshakeUtils.PIECE_MAX_LENGTH + 4个byte头部length + 1个byte的id + 4个byte的index + 4个byte的begin
     * {@link xyz.zzyitj.nbt.util.PeerWireConst#PIECE}
     */
    public static final int FRAME_MAX_LENGTH = HandshakeUtils.PIECE_MAX_LENGTH + 13;

}

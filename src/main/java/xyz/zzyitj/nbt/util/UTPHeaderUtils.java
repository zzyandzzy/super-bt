package xyz.zzyitj.nbt.util;

import xyz.zzyitj.nbt.bean.UTPHeader;

/**
 * xyz.zzyitj.nbt.util
 * <p>
 * http://www.bittorrent.org/beps/bep_0029.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 4:09 下午
 * @since 1.0
 */
public class UTPHeaderUtils {
    /**
     * 具体可以看
     * http://www.bittorrent.org/beps/bep_0029.html#type
     * 数据帧
     * regular data packet. Socket is in connected state and has data to send.
     * An ST_DATA packet always has a data payload.
     */
    public static final byte HEADER_TYPE_DATA = 0x0;
    /**
     * 断开连接帧
     * Finalize the connection.
     * This is the last packet. It closes the connection, similar to TCP FIN flag.
     * This connection will never have a sequence number greater than the sequence number in this packet.
     * The socket records this sequence number as eof_pkt.
     * This lets the socket wait for packets that might still be missing and arrive out of order even after receiving the ST_FIN packet.
     */
    public static final byte HEADER_TYPE_FIN = 0x10;
    /**
     * 确认帧
     * State packet.
     * Used to transmit an ACK with no data.
     * Packets that don't include any payload do not increase the seq_nr.
     */
    public static final byte HEADER_TYPE_STATE = 0x20;
    /**
     * 重建连接帧
     * Terminate connection forcefully.
     * Similar to TCP RST flag.
     * The remote host does not have any state for this connection.
     * It is stale and should be terminated.
     */
    public static final byte HEADER_TYPE_RESET = 0x30;
    /**
     * 建立连接帧
     * Connect SYN.
     * Similar to TCP SYN flag, this packet initiates a connection.
     * The sequence number is initialized to 1.
     * The connection ID is initialized to a random number.
     * The syn packet is special, all subsequent packets sent on this connection (except for re-sends of the ST_SYN) are sent with the connection ID + 1.
     * The connection ID is what the other end is expected to use in its responses.
     * <p>
     * When receiving an ST_SYN, the new socket should be initialized with the ID in the packet header.
     * The send ID for the socket should be initialized to the ID + 1.
     * The sequence number for the return channel is initialized to a random number.
     * The other end expects an ST_STATE packet (only an ACK) in response.
     */
    public static final byte HEADER_TYPE_SYN = 0x40;
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#version
     * 协议版本号，目前是1
     * This is the protocol version. The current version is 1.
     */
    public static final byte HEADER_VERSION = 0x1;
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#extension
     * 扩展字段，比如selective ack就需要这个字段非零，没有扩展置零即可。
     */
    public static final byte HEADER_EXTENSION = 0x0;
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#connection-id
     * <p>
     * 用来唯一表示一个连接的，一个connection会使用两个id，一个用来发，一个用来收，注意本端的收=对端的发，所以一个connection有两个id，
     * 而变量一共有四个：一端的收发两个，另一端的收发两个。
     * 而一端的收发id只相差1（连接发起一端随机产生一个数，作为接收id，此端发送id=接收id+1），
     * 所以可以用这个id来唯一标识一个connection。
     * <p>
     * This is a random, unique, number identifying all the packets that belong to the same connection.
     * Each socket has one connection ID for sending packets and a different connection ID for receiving packets.
     * The endpoint initiating the connection decides which ID to use, and the return path has the same ID + 1.
     */
    public static final byte[] HEADER_CONNECTION_ID = {0x0, 0x0};
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#timestamp-microseconds
     * 表示此帧的发送时间。
     * This is the 'microseconds' parts of the timestamp of when this packet was sent.
     * This is set using gettimeofday() on posix and QueryPerformanceTimer() on windows.
     * The higher resolution this timestamp has, the better.
     * The closer to the actual transmit time it is set, the better.
     */
    public static final byte[] HEADER_TIMESTAMP_MICROSECONDS = {0x0, 0x0, 0x0, 0x0};
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#timestamp-difference-microseconds
     * 单向传播时延，用帧的收到时间-timestamp_microseconds的时间。
     * 由于两端的时间并没有同步，或者说并没有到达微秒级别的同步，这个字段的绝对值并没有实际意义，但是对比两次timestamp_difference_microseconds的差别具有意义。
     * This is the difference between the local time and the timestamp in the last received packet, at the time the last packet was received.
     * This is the latest one-way delay measurement of the link from the remote peer to the local machine.
     * <p>
     * When a socket is newly opened and doesn't have any delay samples yet, this must be set to 0.
     */
    public static final byte[] HEADER_TIMESTAMP_DIFFERENCE_MICROSECONDS = {0x0, 0x0, 0x0, 0x0};
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#end-size
     * 窗口大小，这个和TCP里面的意义一样，是对端给的一个限制发送的标志，用于流量控制。
     * Advertised receive window. This is 32 bits wide and specified in bytes.
     * <p>
     * The window size is the number of bytes currently in-flight, i.e. sent but not acked.
     * The advertised receive window lets the other end cap the window size if it cannot receive any faster, if its receive buffer is filling up.
     * <p>
     * When sending packets, this should be set to the number of bytes left in the socket's receive buffer.
     */
    public static final byte[] HEADER_WND_SIZE = {0x0, 0x0, 0x0, 0x0};
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#seq-nr
     * 包序号。这个和TCP一样，双方各自维护一个序列来表示自己的发送。
     * This is the sequence number of this packet.
     * As opposed to TCP, uTP sequence numbers are not referring to bytes, but packets.
     * The sequence number tells the other end in which order packets should be served back to the application layer.
     */
    public static final byte[] HEADER_SEQ_NR = {0x0, 0x0};
    /**
     * http://www.bittorrent.org/beps/bep_0029.html#ack-nr
     * 确认号。注意这里和TCP不同，TCP是应答下一个期望得到的对方的seq。而这里是收到了哪个seq就ack那个seq。
     * This is the sequence number the sender of the packet last received in the other direction.
     */
    public static final byte[] HEADER_ACK_NR = {0x0, 0x0};

    /**
     * http://www.bittorrent.org/beps/bep_0029.html#header-format
     * header format
     * version 1 header:
     * <p>
     * 0       4       8               16              24              32
     * +-------+-------+---------------+---------------+---------------+
     * | type  | ver   | extension     | connection_id                 |
     * +-------+-------+---------------+---------------+---------------+
     * | timestamp_microseconds                                        |
     * +---------------+---------------+---------------+---------------+
     * | timestamp_difference_microseconds                             |
     * +---------------+---------------+---------------+---------------+
     * | wnd_size                                                      |
     * +---------------+---------------+---------------+---------------+
     * | seq_nr                        | ack_nr                        |
     * +---------------+---------------+---------------+---------------+
     *
     * @return header
     */
    public static UTPHeader buildInitHeader() {
        UTPHeader utpHeader = new UTPHeader();
        utpHeader.setConnectionId(ByteUtils.getRandBytes(2));
        utpHeader.setTimestampMicroseconds(ByteUtils.intToBytesBigEndian((int) System.currentTimeMillis()));
        utpHeader.setSeqNr(ByteUtils.getRandBytes(2));
        return utpHeader;
    }

    public static byte[] buildInitHeaderBytes() {
        return utpHeaderToBytes(buildInitHeader());
    }

    public static byte[] utpHeaderToBytes(UTPHeader header) {
        byte[] headerBytes = new byte[]{HEADER_TYPE_SYN | HEADER_VERSION,
                // extension
                HEADER_EXTENSION,
                // connection_id
                0x0, 0x0,
                // timestamp_microseconds
                0x0, 0x0, 0x0, 0x0,
                // timestamp_difference_microseconds
                0x0, 0x0, 0x0, 0x0,
                // wnd_size
                0x0, 0x0, 0x0, 0x0,
                // seq_nr
                0x0, 0x0,
                // ack_nr
                0x0, 0x0
        };
        headerBytes[0] = (byte) (header.getType() | header.getVersion());
        headerBytes[1] = header.getExtension();
        System.arraycopy(header.getConnectionId(), 0, headerBytes, 2, header.getConnectionId().length);
        System.arraycopy(header.getTimestampMicroseconds(), 0, headerBytes,
                4, header.getTimestampMicroseconds().length);
        System.arraycopy(header.getTimestampDifferenceMicroseconds(), 0, headerBytes,
                8, header.getTimestampDifferenceMicroseconds().length);
        System.arraycopy(header.getWndSize(), 0, headerBytes, 12, header.getWndSize().length);
        System.arraycopy(header.getSeqNr(), 0, headerBytes, 16, header.getSeqNr().length);
        System.arraycopy(header.getAckNr(), 0, headerBytes, 18, header.getAckNr().length);
        return headerBytes;
    }
}

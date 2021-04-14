package xyz.zzyitj.nbt.protocol.utp.entity;

import lombok.Data;
import xyz.zzyitj.nbt.protocol.utp.constant.UtpProtocolConst;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.RandomUtils;

/**
 * <p>
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
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 6:38 下午
 * @since 1.0
 */
@Data
public class UtpHeader {
    private byte type;
    private byte version;
    private byte extension;
    private short connectionId;
    private short sendConnectionId;
    private short receiveConnectionId;
    private int timestampMicroseconds;
    private int timestampDifferenceMicroseconds;
    private int wndSize;
    private short seqNr;
    private short ackNr;
    private byte[] payload;

    public UtpHeader() {
        version = UtpProtocolConst.VERSION;
        type = UtpProtocolConst.TYPE_SYN;
        extension = UtpProtocolConst.EXTENSION;
        receiveConnectionId = RandomUtils.getRandShort();
        sendConnectionId = (short) (receiveConnectionId + 1);
        connectionId = receiveConnectionId;
        timestampMicroseconds = (int) System.currentTimeMillis();
        timestampDifferenceMicroseconds = 0;
        wndSize = 0;
//        seqNr = RandomUtils.getRandShort();
        seqNr = 1;
        ackNr = 0;
        payload = new byte[0];
    }

    public byte[] getSendConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(sendConnectionId);
    }

    public byte[] getReceiveConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(receiveConnectionId);
    }

    public byte[] getTimestampMicrosecondsBytes() {
        return ByteUtils.intToBytesBigEndian(timestampMicroseconds);
    }

    public byte[] getTimestampDifferenceMicrosecondsBytes() {
        return ByteUtils.intToBytesBigEndian(timestampDifferenceMicroseconds);
    }

    public byte[] getWndSizeBytes() {
        return ByteUtils.intToBytesBigEndian(wndSize);
    }

    public byte[] getSeqNrBytes() {
        return ByteUtils.shortToBytesBigEndian(seqNr);
    }

    public byte[] getAckNrBytes() {
        return ByteUtils.shortToBytesBigEndian(ackNr);
    }

    public byte[] getConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(connectionId);
    }
}

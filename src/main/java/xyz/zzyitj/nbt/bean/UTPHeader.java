package xyz.zzyitj.nbt.bean;

import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.RandomUtils;
import xyz.zzyitj.nbt.util.UTPHeaderUtils;

import java.util.Arrays;

/**
 * xyz.zzyitj.nbt.bean
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
public class UTPHeader {
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

    public UTPHeader() {
        version = UTPHeaderUtils.HEADER_VERSION;
        type = UTPHeaderUtils.HEADER_TYPE_SYN;
        extension = UTPHeaderUtils.HEADER_EXTENSION;
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

    @Override
    public String toString() {
        return "UTPHeader{" +
                "type=" + type +
                ", version=" + version +
                ", extension=" + extension +
                ", connectionId=" + connectionId +
                ", sendConnectionId=" + sendConnectionId +
                ", receiveConnectionId=" + receiveConnectionId +
                ", timestampMicroseconds=" + timestampMicroseconds +
                ", timestampDifferenceMicroseconds=" + timestampDifferenceMicroseconds +
                ", wndSize=" + wndSize +
                ", seqNr=" + seqNr +
                ", ackNr=" + ackNr +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getExtension() {
        return extension;
    }

    public void setExtension(byte extension) {
        this.extension = extension;
    }

    public short getSendConnectionId() {
        return sendConnectionId;
    }

    public byte[] getSendConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(sendConnectionId);
    }

    public void setSendConnectionId(short sendConnectionId) {
        this.sendConnectionId = sendConnectionId;
    }

    public short getReceiveConnectionId() {
        return receiveConnectionId;
    }

    public byte[] getReceiveConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(receiveConnectionId);
    }

    public void setReceiveConnectionId(short receiveConnectionId) {
        this.receiveConnectionId = receiveConnectionId;
    }

    public int getTimestampMicroseconds() {
        return timestampMicroseconds;
    }

    public byte[] getTimestampMicrosecondsBytes() {
        return ByteUtils.intToBytesBigEndian(timestampMicroseconds);
    }

    public void setTimestampMicroseconds(int timestampMicroseconds) {
        this.timestampMicroseconds = timestampMicroseconds;
    }

    public int getTimestampDifferenceMicroseconds() {
        return timestampDifferenceMicroseconds;
    }

    public byte[] getTimestampDifferenceMicrosecondsBytes() {
        return ByteUtils.intToBytesBigEndian(timestampDifferenceMicroseconds);
    }

    public void setTimestampDifferenceMicroseconds(int timestampDifferenceMicroseconds) {
        this.timestampDifferenceMicroseconds = timestampDifferenceMicroseconds;
    }

    public int getWndSize() {
        return wndSize;
    }

    public byte[] getWndSizeBytes() {
        return ByteUtils.intToBytesBigEndian(wndSize);
    }

    public void setWndSize(int wndSize) {
        this.wndSize = wndSize;
    }

    public short getSeqNr() {
        return seqNr;
    }

    public byte[] getSeqNrBytes() {
        return ByteUtils.shortToBytesBigEndian(seqNr);
    }

    public void setSeqNr(short seqNr) {
        this.seqNr = seqNr;
    }

    public short getAckNr() {
        return ackNr;
    }

    public byte[] getAckNrBytes() {
        return ByteUtils.shortToBytesBigEndian(ackNr);
    }

    public void setAckNr(short ackNr) {
        this.ackNr = ackNr;
    }

    public short getConnectionId() {
        return connectionId;
    }

    public byte[] getConnectionIdBytes() {
        return ByteUtils.shortToBytesBigEndian(connectionId);
    }

    public void setConnectionId(short connectionId) {
        this.connectionId = connectionId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}

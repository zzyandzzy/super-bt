package xyz.zzyitj.nbt.bean;

import xyz.zzyitj.nbt.util.UTPHeaderUtils;

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
    private byte[] connectionId;
    private byte[] timestampMicroseconds;
    private byte[] timestampDifferenceMicroseconds;
    private byte[] wndSize;
    private byte[] seqNr;
    private byte[] ackNr;

    public UTPHeader() {
        type = UTPHeaderUtils.HEADER_TYPE_SYN;
        version = UTPHeaderUtils.HEADER_VERSION;
        extension = UTPHeaderUtils.HEADER_EXTENSION;
        connectionId = UTPHeaderUtils.HEADER_CONNECTION_ID;
        timestampMicroseconds = UTPHeaderUtils.HEADER_TIMESTAMP_MICROSECONDS;
        timestampDifferenceMicroseconds = UTPHeaderUtils.HEADER_TIMESTAMP_DIFFERENCE_MICROSECONDS;
        wndSize = UTPHeaderUtils.HEADER_WND_SIZE;
        seqNr = UTPHeaderUtils.HEADER_SEQ_NR;
        ackNr = UTPHeaderUtils.HEADER_ACK_NR;
    }

    public UTPHeader(byte type, byte extension, byte[] connectionId,
                     byte[] timestampMicroseconds,
                     byte[] timestampDifferenceMicroseconds,
                     byte[] wndSize,
                     byte[] seqNr, byte[] ackNr) {
        this.type = type;
        this.version = UTPHeaderUtils.HEADER_VERSION;
        this.extension = extension;
        this.connectionId = connectionId;
        this.timestampMicroseconds = timestampMicroseconds;
        this.timestampDifferenceMicroseconds = timestampDifferenceMicroseconds;
        this.wndSize = wndSize;
        this.seqNr = seqNr;
        this.ackNr = ackNr;
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

    public byte[] getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(byte[] connectionId) {
        this.connectionId = connectionId;
    }

    public byte[] getTimestampMicroseconds() {
        return timestampMicroseconds;
    }

    public void setTimestampMicroseconds(byte[] timestampMicroseconds) {
        this.timestampMicroseconds = timestampMicroseconds;
    }

    public byte[] getTimestampDifferenceMicroseconds() {
        return timestampDifferenceMicroseconds;
    }

    public void setTimestampDifferenceMicroseconds(byte[] timestampDifferenceMicroseconds) {
        this.timestampDifferenceMicroseconds = timestampDifferenceMicroseconds;
    }

    public byte[] getWndSize() {
        return wndSize;
    }

    public void setWndSize(byte[] wndSize) {
        this.wndSize = wndSize;
    }

    public byte[] getSeqNr() {
        return seqNr;
    }

    public void setSeqNr(byte[] seqNr) {
        this.seqNr = seqNr;
    }

    public byte[] getAckNr() {
        return ackNr;
    }

    public void setAckNr(byte[] ackNr) {
        this.ackNr = ackNr;
    }
}

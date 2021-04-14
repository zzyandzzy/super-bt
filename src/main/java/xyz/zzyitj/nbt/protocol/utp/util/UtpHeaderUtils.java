package xyz.zzyitj.nbt.protocol.utp.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import xyz.zzyitj.nbt.protocol.utp.constant.UtpProtocolConst;
import xyz.zzyitj.nbt.protocol.utp.entity.UtpHeader;

/**
 * <p>
 * http://www.bittorrent.org/beps/bep_0029.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 4:09 下午
 * @since 1.0
 */
public class UtpHeaderUtils {

    public static UtpHeader buildInitHeader() {
        return new UtpHeader();
    }

    public static byte[] buildInitHeaderBytes() {
        return utpHeaderToBytes(buildInitHeader());
    }

    /**
     * 把UTPHeader转换成字节数组
     * //        byte[] headerBytes = new byte[]{0x0,
     * //                // extension
     * //                0x0,
     * //                // connection_id
     * //                0x0, 0x0,
     * //                // timestamp_microseconds
     * //                0x0, 0x0, 0x0, 0x0,
     * //                // timestamp_difference_microseconds
     * //                0x0, 0x0, 0x0, 0x0,
     * //                // wnd_size
     * //                0x0, 0x0, 0x0, 0x0,
     * //                // seq_nr
     * //                0x0, 0x0,
     * //                // ack_nr
     * //                0x0, 0x0
     * //                // payload
     * //        };
     *
     * @param header header
     * @return header bytes
     */
    public static byte[] utpHeaderToBytes(UtpHeader header) {
        byte[] headerBytes = new byte[UtpProtocolConst.HEADER_LENGTH + header.getPayload().length];
        headerBytes[0] = (byte) (header.getType() | header.getVersion());
        headerBytes[1] = header.getExtension();
        System.arraycopy(header.getConnectionIdBytes(), 0,
                headerBytes, 2, header.getConnectionIdBytes().length);
        System.arraycopy(header.getTimestampMicrosecondsBytes(), 0,
                headerBytes, 4, header.getTimestampMicrosecondsBytes().length);
        System.arraycopy(header.getTimestampDifferenceMicrosecondsBytes(), 0,
                headerBytes, 8, header.getTimestampDifferenceMicrosecondsBytes().length);
        System.arraycopy(header.getWndSizeBytes(), 0, headerBytes, 12, header.getWndSizeBytes().length);
        System.arraycopy(header.getSeqNrBytes(), 0, headerBytes, 16, header.getSeqNrBytes().length);
        System.arraycopy(header.getAckNrBytes(), 0, headerBytes, 18, header.getAckNrBytes().length);
        if (header.getPayload() != null && header.getPayload().length != 0) {
            System.arraycopy(header.getPayload(), 0, headerBytes, UtpProtocolConst.HEADER_LENGTH, header.getPayload().length);
        }
        return headerBytes;
    }

    /**
     * 把bytes转换成UTPHeader
     *
     * @param data bytes
     * @return UTPHeader
     */
    public static UtpHeader bytesToUtpHeader(byte[] data) {
        return bytesToUtpHeader(data, 0);
    }

    private static UtpHeader bytesToUtpHeader(byte[] data, int start) {
        if (start + UtpProtocolConst.HEADER_LENGTH > data.length) {
            return null;
        }
        UtpHeader utpHeader = new UtpHeader();
        utpHeader.setType((byte) (data[start] & 0xF0));
        utpHeader.setVersion((byte) (data[start] & 0xF));
        ByteBuf buf = Unpooled.copiedBuffer(data, start + 1, 19);
        utpHeader.setExtension(buf.readByte());
        utpHeader.setConnectionId(buf.readShort());
        utpHeader.setSendConnectionId((short) (utpHeader.getConnectionId() + 1));
        utpHeader.setReceiveConnectionId(utpHeader.getConnectionId());
        utpHeader.setTimestampMicroseconds(buf.readInt());
        utpHeader.setTimestampDifferenceMicroseconds(buf.readInt());
        utpHeader.setWndSize(buf.readInt());
        utpHeader.setSeqNr(buf.readShort());
        utpHeader.setAckNr(buf.readShort());
        buf.release();
        return utpHeader;
    }
}

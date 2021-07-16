package xyz.zzyitj.nbt.protocol.utp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.protocol.utp.constant.UtpProtocolConst;
import xyz.zzyitj.nbt.protocol.utp.entity.UtpHeader;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.protocol.utp.util.UtpHeaderUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 处理UTP客户端情况
 * <p>
 * Here is a diagram illustrating the exchanges and states to initiate a connection.
 * The c.* refers to a state in the socket itself,
 * pkt.* refers to a field in the packet header.
 * <p>
 * initiating endpoint              accepting endpoint
 * <p>
 * | c.state = CS_SYN_SENT                         |
 * | c.seq_nr = 1                                  |
 * | c.conn_id_recv = rand()                       | = 0
 * | c.conn_id_send = c.conn_id_recv + 1           | = 1
 * |                                               |
 * |                                               |
 * | ST_SYN                                        |
 * |   seq_nr=c.seq_nr++                           |
 * |   ack_nr=*                                    |
 * |   conn_id=c.rcv_conn_id                       | = 0
 * | >-------------------------------------------> |
 * |             c.receive_conn_id = pkt.conn_id+1 | = 1
 * |             c.send_conn_id = pkt.conn_id      | = 0
 * |             c.seq_nr = rand()                 |
 * |             c.ack_nr = pkt.seq_nr             |
 * |             c.state = CS_SYN_RECV             |
 * |                                               |
 * |                                               |
 * |                                               |
 * |                                               |
 * |                     ST_STATE                  |
 * |                       seq_nr=c.seq_nr++       |
 * |                       ack_nr=c.ack_nr         |
 * |                       conn_id=c.send_conn_id  | = 0
 * | <------------------------------------------<  |
 * | c.state = CS_CONNECTED                        |
 * | c.ack_nr = pkt.seq_nr                         |
 * |                                               |
 * |                                               |
 * |                                               |
 * | ST_DATA                                       |
 * |   seq_nr=c.seq_nr++                           |
 * |   ack_nr=c.ack_nr                             |
 * |   conn_id=c.conn_id_send                      | = 1
 * | >-------------------------------------------> |
 * |                        c.ack_nr = pkt.seq_nr  |
 * |                        c.state = CS_CONNECTED |
 * |                                               |
 * |                                               | connection established
 * .. ..|.. .. .. .. .. .. .. .. .. .. .. .. .. .. .. ..|.. ..
 * |                                               |
 * |                     ST_DATA                   |
 * |                       seq_nr=c.seq_nr++       |
 * |                       ack_nr=c.ack_nr         |
 * |                       conn_id=c.send_conn_id  | = 0
 * | <------------------------------------------<  |
 * | c.ack_nr = pkt.seq_nr                         |
 * |                                               |
 * |                                               |
 * V                                               V
 * Connections are identified by their conn_id header. If the connection ID of a new connection collides with an existing connection,
 * the connection attempt will fails, since the ST_SYN packet will be unexpected in the existing stream, and ignored.
 *
 * <a herf="http://www.bittorrent.org/beps/bep_0029.html#connection-setup"></a>
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 3:29 下午
 * @since 1.0
 */
public abstract class AbstractUtpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractUtpHandler.class);
    /**
     * 种子信息
     */
    protected Torrent torrent;
    /**
     * 做种的种子
     */
    protected List<Torrent> torrentList;
    /**
     * 下载管理器
     */
    protected AbstractDownloadManager downloadManager;
    /**
     * utp头部
     */
    protected UtpHeader header;
    /**
     * 当前连接状态位
     * 对比TCP三次握手的：
     * CLOSE、SYN_SENT、SYN_RECV、ESTABLISHED、LISTEN
     */
    protected byte state = UtpProtocolConst.CS_CLOSE;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (torrent != null) {
            state = UtpProtocolConst.CS_SYN_SENT;
            header = UtpHeaderUtils.buildInitHeader();
            // 打开socket就发送握手
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(UtpHeaderUtils.utpHeaderToBytes(header)),
                    (InetSocketAddress) ctx.channel().remoteAddress()));
            logger.info("Client: {} send init utp header.", ctx.channel().remoteAddress());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        if (data.length >= UtpProtocolConst.HEADER_LENGTH) {
            UtpHeader receiveHeader = UtpHeaderUtils.bytesToUtpHeader(data);
            header.setTimestampMicroseconds((int) System.currentTimeMillis());
            header.setTimestampDifferenceMicroseconds((int) (System.currentTimeMillis() - receiveHeader.getTimestampMicroseconds()));
            header.setWndSize((int) (ByteUtils.BYTE_KB));
            // connectionId相同并且是确认帧说明是建立连接过程
            if (receiveHeader.getType() == UtpProtocolConst.TYPE_STATE
                    && receiveHeader.getConnectionId() == header.getConnectionId()) {
                // 建立连接了！！！
                state = UtpProtocolConst.CS_CONNECTED;
                header.setType((byte) (UtpProtocolConst.TYPE_DATA | UtpProtocolConst.VERSION));
                header.setConnectionId(header.getSendConnectionId());
                header.setSeqNr((short) (header.getSeqNr() + 1));
                header.setAckNr((short) (receiveHeader.getSeqNr() - 1));
                ctx.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(UtpHeaderUtils.utpHeaderToBytes(header)), msg.sender()));
            } else if (receiveHeader.getType() == UtpProtocolConst.TYPE_FIN
                    && receiveHeader.getConnectionId() == header.getReceiveConnectionId()) {
                // 断开连接
                state = UtpProtocolConst.CS_CLOSE;
                header.setType((byte) (UtpProtocolConst.TYPE_FIN | UtpProtocolConst.VERSION));
                header.setSeqNr((short) (receiveHeader.getAckNr() + 1));
                header.setAckNr(receiveHeader.getSeqNr());
                ctx.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(UtpHeaderUtils.utpHeaderToBytes(header)), msg.sender()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

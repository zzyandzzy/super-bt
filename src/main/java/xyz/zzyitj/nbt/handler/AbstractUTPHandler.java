package xyz.zzyitj.nbt.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.bean.UTPHeader;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.UTPHeaderUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 处理UTP客户端情况
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 3:29 下午
 * @since 1.0
 */
public abstract class AbstractUTPHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractUTPHandler.class);
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
    protected UTPHeader header;
    /**
     * 是否是第一次发送握手消息
     */
    private boolean isFirstWriteHandshake = true;
    /**
     * 是否是第一次收到握手消息
     */
    private boolean isFirstReadHandshake = true;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (torrent != null) {
            header = UTPHeaderUtils.buildInitHeader();
            // 打开socket就发送握手
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(UTPHeaderUtils.utpHeaderToBytes(header)),
                    (InetSocketAddress) ctx.channel().remoteAddress()));
            logger.info("Client: {} send init utp header.", ctx.channel().remoteAddress());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        if (data.length >= UTPHeaderUtils.HEADER_LENGTH) {
            UTPHeader receiveHeader = UTPHeaderUtils.bytesToUtpHeader(data);
            header.setTimestampMicroseconds((int) System.currentTimeMillis());
            header.setTimestampDifferenceMicroseconds((int) (System.currentTimeMillis() - receiveHeader.getTimestampMicroseconds()));
            header.setWndSize((int) (ByteUtils.BYTE_KB));
            // connectionId相同并且是确认帧说明是建立连接过程
            if (receiveHeader.getType() == UTPHeaderUtils.TYPE_STATE
                    && receiveHeader.getConnectionId() == header.getConnectionId()) {
                header.setType((byte) (UTPHeaderUtils.TYPE_DATA | UTPHeaderUtils.VERSION));
                header.setConnectionId(header.getSendConnectionId());
                header.setSeqNr((short) (header.getSeqNr() + 1));
                header.setAckNr((short) (receiveHeader.getSeqNr() - 1));
                ctx.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(UTPHeaderUtils.utpHeaderToBytes(header)), msg.sender()));
            } else if (receiveHeader.getType() == UTPHeaderUtils.TYPE_FIN
                    && receiveHeader.getConnectionId() == header.getReceiveConnectionId()) {
                // 说明是断开连接帧
                header.setType((byte) (UTPHeaderUtils.TYPE_FIN | UTPHeaderUtils.VERSION));
                header.setSeqNr((short) (receiveHeader.getAckNr() + 1));
                header.setAckNr(receiveHeader.getSeqNr());
                ctx.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(UTPHeaderUtils.utpHeaderToBytes(header)), msg.sender()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

package xyz.zzyitj.nbt.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.util.Arrays;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/15 2:45 下午
 * @email zzy.main@gmail.com
 */
public class BTClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final byte[] infoHash;

    public BTClientHandler(byte[] infoHash) {
        this.infoHash = infoHash;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 打开socket就发送握手
        ctx.writeAndFlush(Unpooled.copiedBuffer(
                HandshakeUtils.buildHandshake(infoHash, Const.TEST_PEER_ID)));
        System.out.println("buildHandshake");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] data = new byte[msg.readableBytes()];
        msg.getBytes(0, data);
        // 如果peer同意了我们的握手，说明该peer有该info_hash的文件在做种
        if (HandshakeUtils.isHandshake(data)) {
            // 真正的开发中要在上面判断对该peer是否感兴趣
            // 发送对此peer感兴趣
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.buildMessage(HandshakeUtils.INTERESTED)));
            System.out.println("buildInterested");
        } else {
            switch (data[HandshakeUtils.PEER_WIRE_ID_INDEX]) {
                case HandshakeUtils.CHOKE:
                    closePeer(ctx);
                    break;
                case HandshakeUtils.UN_CHOKE:
                    // peer发送UN_CHOKE之后就可以下载区块了
                    System.out.println("UN_CHOKE");
                    System.out.println("request download begin: 0, length: 16384");
                    ctx.writeAndFlush(Unpooled.copiedBuffer(
                            HandshakeUtils.bitFieldHandler()));
                    break;
                case HandshakeUtils.BIT_FIELD:
                    PeerWire<byte[]> peerWire = HandshakeUtils.parsePeerWire(data);
                    // 先验证消息长度是否正确
                    if (HandshakeUtils.isBitField(peerWire)) {
                        // 检查UN_CHOKE状态并生成要下载的区块
                    } else {
                        closePeer(ctx);
                    }
                    break;
                case HandshakeUtils.PIECE:
                    break;
                default:
                    // 其他情况
                    System.out.println("other.");
            }
        }
    }

    /**
     * 关闭连接
     *
     * @param ctx ctx
     */
    private void closePeer(ChannelHandlerContext ctx) {
        // 关闭这个peer
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 连接关闭，可能情况：
     * 1、IP+Port无法连接
     * 2、如果一个客户端接收到一个握手报文，并且该客户端没有服务这个报文的info_hash，那么该客户端必须丢弃该连接。
     * 3、如果一个连接发起者接收到一个握手报文，并且该报文中peer_id与期望的peer_id不匹配，那么连接发起者应该丢弃该连接。
     * 4、peer发送keep-alive（00 00 00 00 00 00 00 00）没有进行回答，2分钟后自动关闭
     *
     * @param ctx ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + " close.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

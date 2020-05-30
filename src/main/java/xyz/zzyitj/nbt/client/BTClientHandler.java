package xyz.zzyitj.nbt.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.HandshakeUtils;

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
        System.out.println("generateHandshake");
        ctx.writeAndFlush(Unpooled.copiedBuffer(HandshakeUtils.generateHandshake(infoHash, Const.TEST_PEER_ID)));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] data = new byte[msg.readableBytes()];
        msg.getBytes(0, data);
        // 如果peer同意了我们的握手，说明该peer有该info_hash的文件在做种
        if (HandshakeUtils.isHandshake(data)) {
            System.out.println("generateInterested");
            ctx.writeAndFlush(Unpooled.copiedBuffer(HandshakeUtils.generateMessage(HandshakeUtils.INTERESTED)));
        } else {
            // 其他情况
        }
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

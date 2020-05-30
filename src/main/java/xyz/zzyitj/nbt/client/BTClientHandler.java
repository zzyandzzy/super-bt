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
        if (HandshakeUtils.isHandshake(data)) {
            System.out.println("generateInterested");
            ctx.writeAndFlush(Unpooled.copiedBuffer(HandshakeUtils.generateInterested()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

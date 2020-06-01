package xyz.zzyitj.nbt.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.FileUtils;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.File;
import java.io.IOException;


/**
 * @author intent
 * @version 1.0
 * @date 2020/3/15 2:45 下午
 * @email zzy.main@gmail.com
 */
public class BTClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    /**
     * 种子信息
     */
    private final Torrent torrent;
    /**
     * 文件下载位置
     */
    private final String savePath;
    /**
     * 是否允许下载
     */
    private boolean unChoke = false;
    /**
     * 是否是第一次握手
     */
    private boolean isFirstHandshake = true;

    public BTClientHandler(Torrent torrent, String savePath) {
        this.torrent = torrent;
        this.savePath = savePath;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 打开socket就发送握手
        ctx.writeAndFlush(Unpooled.copiedBuffer(
                HandshakeUtils.buildHandshake(torrent.getInfoHash(), Const.TEST_PEER_ID)));
        System.out.println("buildHandshake");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // 如果peer同意了我们的握手，说明该peer有该info_hash的文件在做种
        if (isFirstHandshake) {
            byte[] data = new byte[HandshakeUtils.BIT_TORRENT_PROTOCOL.length];
            msg.getBytes(1, data, 0, HandshakeUtils.BIT_TORRENT_PROTOCOL.length);
            if (HandshakeUtils.isHandshake(data)) {
                // 还可以在上面判断对该peer是否感兴趣
                isFirstHandshake = false;
                // 发送对此peer感兴趣
                ctx.writeAndFlush(Unpooled.copiedBuffer(
                        HandshakeUtils.buildMessage(HandshakeUtils.INTERESTED)));
                System.out.println("buildInterested");
                // 如果收到的数据长度大于68，则进行拆分数组
                if (msg.readableBytes() > HandshakeUtils.HANDSHAKE_LENGTH) {
                    splitData(ctx, msg, HandshakeUtils.HANDSHAKE_LENGTH);
                }
            } else {
                // 不是bt协议，关闭连接
                closePeer(ctx);
            }
        } else {
            splitData(ctx, msg, 0);
        }
    }

    /**
     * 循环从index处分割数组
     *
     * @param ctx   ctx
     * @param msg   msg
     * @param index index
     */
    private void splitData(ChannelHandlerContext ctx, ByteBuf msg, int index) {
        int length = msg.readableBytes() - index;
        byte[] data = new byte[length];
        msg.getBytes(index, data, 0, length);
        int start = 0;
        while (true) {
            // 取出data前4位转换为10进制
            int size = ByteUtils.bytesToInt(data, start, start + 3) + 4;
            doHandshakeHandler(ctx, data, start, size);
            System.out.println("start: " + start + ", size: " + size
                    + ", data: [" + data[start] + ", " + data[start + 1] + ", "
                    + data[start + 2] + ", " + data[start + 3] + ", " + data[start + 4] + "]");
            if (start + size >= data.length) {
                return;
            }
            start = size;
        }
    }

    /**
     * 根据peer返回的数据判断下一步操作
     *
     * @param ctx    ctx
     * @param data   字节数组
     * @param start  字节数组开始位置
     * @param length 字节数组开始位置之后的长度
     */
    private void doHandshakeHandler(ChannelHandlerContext ctx, byte[] data, int start, int length) {
        switch (data[start + HandshakeUtils.PEER_WIRE_ID_INDEX]) {
            case HandshakeUtils.CHOKE:
                closePeer(ctx);
                break;
            case HandshakeUtils.UN_CHOKE:
                unChokeHandler(ctx, data, start, length);
                break;
            case HandshakeUtils.BIT_FIELD:
                bitFieldHandler(ctx, data, start, length);
                break;
            case HandshakeUtils.PIECE:
                // 这里就可以保存文件了
//                try {
//                    FileUtils.writeByteArrayToFile(
//                            new File(savePath + torrent.getName()),
//                            data, start + 13, length - 13, true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (data.length - start - 13 >= torrent.getTorrentLength()) {
                    System.out.printf("download %s complete!\n", torrent.getName());
                    closePeer(ctx);
                    return;
                }
                break;
            default:
                // 其他情况
                System.out.println("other.");
        }
    }

    /**
     * 生成区块下载队列
     *
     * @param ctx    ctx
     * @param data   字节数组
     * @param start  字节数组开始位置
     * @param length 字节数组开始位置之后的长度
     */
    private void bitFieldHandler(ChannelHandlerContext ctx, byte[] data, int start, int length) {
        PeerWire<byte[]> peerWire = HandshakeUtils.parsePeerWire(data, start, length - 4);
        // 先验证消息长度是否正确
        if (HandshakeUtils.isBitField(peerWire)) {
            System.out.println(peerWire);
            // 检查UN_CHOKE状态并生成要下载的区块
            if (unChoke) {
                // 这里还得检查peer有没有该区块
                // 之后就可以下载区块了
                doDownload(ctx);
            }
        } else {
            closePeer(ctx);
        }
    }

    /**
     * 设置unChoke和开始下载
     *
     * @param ctx    ctx
     * @param data   字节数组
     * @param start  字节数组开始位置
     * @param length 字节数组开始位置之后的长度
     */
    private void unChokeHandler(ChannelHandlerContext ctx, byte[] data, int start, int length) {
        unChoke = true;
        // 之后就可以请求下载区块了
        doDownload(ctx);
    }

    /**
     * 下载第0个块
     *
     * @param ctx ctx
     */
    private void doDownload(ChannelHandlerContext ctx) {
        if (torrent.getTorrentLength() > HandshakeUtils.PIECE_MAX_LENGTH) {
            System.out.println("request download piece: 0 begin: 0, length: " + HandshakeUtils.PIECE_MAX_LENGTH);
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, 0, HandshakeUtils.PIECE_MAX_LENGTH)));
        } else {
            System.out.println("request download piece: 0 begin: 0, length: " + torrent.getTorrentLength());
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, 0, torrent.getTorrentLength())));
        }
    }

    /**
     * 关闭连接
     *
     * @param ctx ctx
     */
    private void closePeer(ChannelHandlerContext ctx) {
        unChoke = false;
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

package xyz.zzyitj.nbt.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.HandshakeUtils;
import xyz.zzyitj.nbt.util.PeerWireConst;

import java.io.File;
import java.io.IOException;


/**
 * 处理TCP客户端情况
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/3/15 2:45 下午
 * @since 1.0
 */
public class TCPClientHandler extends ChannelInboundHandlerAdapter {
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
     * 是否是第一次发送握手消息
     */
    private boolean isFirstWriteHandshake = true;

    public TCPClientHandler(Torrent torrent, String savePath) {
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        // 如果peer同意了我们的握手，说明该peer有该info_hash的文件在做种
        if (isFirstWriteHandshake) {
            if (HandshakeUtils.isHandshake(data)) {
                // 还可以在上面判断对该peer是否感兴趣
                isFirstWriteHandshake = false;
                // 发送对此peer感兴趣
                ctx.writeAndFlush(Unpooled.copiedBuffer(
                        HandshakeUtils.buildMessage(PeerWireConst.INTERESTED)));
                System.out.println("buildInterested");
            } else {
                // 不是bt协议，关闭连接
                closePeer(ctx);
            }
        } else {
            doHandshakeHandler(ctx, data);
        }
    }

    /**
     * 根据peer返回的数据判断下一步操作
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    private void doHandshakeHandler(ChannelHandlerContext ctx, byte[] data) {
        switch (data[PeerWireConst.PEER_WIRE_ID_INDEX]) {
            case PeerWireConst.CHOKE:
                closePeer(ctx);
                break;
            case PeerWireConst.UN_CHOKE:
                unChokeHandler(ctx, data);
                break;
            case PeerWireConst.BIT_FIELD:
                bitFieldHandler(ctx, data);
                break;
            case PeerWireConst.PIECE:
                // 这里就可以保存文件了
                pieceHandler(ctx, data);
                break;
            default:
                // 其他情况
                System.out.println("other.");
        }
    }

    /**
     * 保存区块内容
     *
     * @param ctx  ctx
     * @param data data
     */
    private void pieceHandler(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        System.out.println(peerWire);
        PeerWirePayload peerWirePayload = (PeerWirePayload) peerWire.getPayload();
        byte[] block = peerWirePayload.getBlock();
        try {
            FileUtils.writeByteArrayToFile(
                    new File(savePath + torrent.getName()),
                    block, 0, block.length, true);
            // 判断是否要继续下载区块
            if (peerWirePayload.getBegin() + block.length >= torrent.getTorrentLength()) {
                System.out.printf("download %s complete!\n", torrent.getName());
                closePeer(ctx);
            } else {
                doDownload(ctx, peerWirePayload.getBegin() + block.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 生成区块下载队列
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    private void bitFieldHandler(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        System.out.println(peerWire);
        // 检查peer下载完的区块并且生成区块下载队列
        if (unChoke) {
            // 之后就可以下载区块了
//            doDownload(ctx);
        }
    }

    /**
     * 设置unChoke和开始下载
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    private void unChokeHandler(ChannelHandlerContext ctx, byte[] data) {
        unChoke = true;
        // 之后就可以请求下载区块了
        doDownload(ctx, 0);
    }

    /**
     * 从第0个块的begin处开始下载
     *
     * @param begin 下载的开始位置
     * @param ctx   ctx
     */
    private void doDownload(ChannelHandlerContext ctx, int begin) {
        if (torrent.getTorrentLength() - begin > HandshakeUtils.PIECE_MAX_LENGTH) {
            System.out.printf("request download index: 0, begin: %d, length: %d\n",
                    begin, HandshakeUtils.PIECE_MAX_LENGTH);
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, begin, HandshakeUtils.PIECE_MAX_LENGTH)));
        } else {
            System.out.printf("request download index: 0, begin: %d, length: %d\n",
                    begin, torrent.getTorrentLength());
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, begin, torrent.getTorrentLength() - begin)));
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

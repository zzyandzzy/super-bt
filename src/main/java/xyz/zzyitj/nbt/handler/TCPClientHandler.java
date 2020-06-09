package xyz.zzyitj.nbt.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.io.FileUtils;
import xyz.zzyitj.nbt.bean.PeerWire;
import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.File;
import java.io.IOException;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 6:58 下午
 * @since 1.0
 */
public class TCPClientHandler extends TCPHandler {

    public TCPClientHandler(TCPClientHandlerBuilder builder) {
        super();
        this.savePath = builder.savePath;
        this.torrent = builder.torrent;
    }

    @Override
    void doChock(ChannelHandlerContext ctx) {
        closePeer(ctx);
    }

    @Override
    void doUnChock(ChannelHandlerContext ctx, byte[] data) {
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
            System.out.printf("Client: request index: 0, begin: %d, length: %d\n",
                    begin, HandshakeUtils.PIECE_MAX_LENGTH);
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, begin, HandshakeUtils.PIECE_MAX_LENGTH)));
        } else {
            System.out.printf("Client: request index: 0, begin: %d, length: %d\n",
                    begin, torrent.getTorrentLength());
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(0, begin, torrent.getTorrentLength() - begin)));
        }
    }

    @Override
    void doInterested() {

    }

    @Override
    void doNotInterested() {

    }

    @Override
    void doHave() {

    }

    /**
     * 生成区块下载队列
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    @Override
    void doBitField(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        System.out.println(peerWire);
        // 检查peer下载完的区块并且生成区块下载队列
        if (unChoke) {
            // 之后就可以下载区块了
//            doDownload(ctx);
        }
    }

    @Override
    void doRequest() {

    }

    /**
     * 保存区块内容
     *
     * @param ctx  ctx
     * @param data data
     */
    @Override
    void doPiece(ChannelHandlerContext ctx, byte[] data) {
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
                System.out.printf("Client: download %s complete!\n", torrent.getName());
                closePeer(ctx);
            } else {
                doDownload(ctx, peerWirePayload.getBegin() + block.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void doCancel() {

    }

    @Override
    void doPort() {

    }

    @Override
    void doExtended() {

    }

    public static class TCPClientHandlerBuilder {
        /**
         * 种子信息
         */
        private Torrent torrent;
        /**
         * 文件下载位置
         */
        private String savePath;

        public TCPHandler build() {
            return new TCPClientHandler(this);
        }

        public TCPClientHandlerBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public TCPClientHandlerBuilder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }
    }
}

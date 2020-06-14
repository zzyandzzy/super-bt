package xyz.zzyitj.nbt.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.util.Queue;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 6:58 下午
 * @since 1.0
 */
public class TCPClientHandler extends AbstractTCPHandler {

    public TCPClientHandler(TCPClientHandlerBuilder builder) {
        super();
        this.torrent = builder.torrent;
        this.downloadManager = builder.downloadManager;
    }

    @Override
    void doChock(ChannelHandlerContext ctx) {
        closePeer(ctx);
    }

    @Override
    void doUnChock(ChannelHandlerContext ctx, byte[] data) {
        unChoke = true;
        // 之后就可以请求下载区块了
        doDownload(ctx);
    }

    /**
     * 从第0个块的begin处开始下载
     *
     * @param ctx ctx
     */
    private void doDownload(ChannelHandlerContext ctx) {
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig != null) {
            Queue<RequestPiece> pieceQueue = downloadConfig.getPieceQueue();
            if (pieceQueue != null) {
                RequestPiece requestPiece = pieceQueue.poll();
                if (requestPiece != null) {
                    ctx.writeAndFlush(Unpooled.copiedBuffer(
                            HandshakeUtils.requestPieceHandler(
                                    new RequestPiece(requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength()))));
                    System.out.printf("Client: request %s, index: %s, begin: %d, length: %d, pieceQueueSize: %d\n",
                            ctx.channel().remoteAddress(),
                            requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength(),
                            pieceQueue.size());
                }
            }
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
     * 处理BitField请求
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    @Override
    void doBitField(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        // 根据peer返回的区块完成信息生成区块下载队列
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig != null && downloadConfig.getPieceQueue() == null) {
            downloadConfig.setOnePieceRequestSum(
                    HandshakeUtils.generateRequestPieceQueue(peerWire, torrent, downloadConfig));
        }
        if (unChoke) {
            // 之后就可以下载区块了
            doDownload(ctx);
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
        PeerWirePayload peerWirePayload = (PeerWirePayload) peerWire.getPayload();
        if (downloadManager.saveBytesToFile(peerWirePayload)) {
            System.out.printf("Client: download %s complete!\n", torrent.getName());
            closePeer(ctx);
        } else {
            doDownload(ctx);
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
         * 下载管理器
         */
        private DownloadManager downloadManager;

        public TCPClientHandlerBuilder(Torrent torrent, DownloadManager downloadManager) {
            this.torrent = torrent;
            this.downloadManager = downloadManager;
            this.downloadManager.setTorrent(torrent);
        }

        public AbstractTCPHandler build() {
            return new TCPClientHandler(this);
        }

        public TCPClientHandlerBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public TCPClientHandlerBuilder downloadManager(DownloadManager downloadManager) {
            this.downloadManager = downloadManager;
            return this;
        }
    }
}

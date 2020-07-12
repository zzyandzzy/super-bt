package xyz.zzyitj.nbt.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(TCPClientHandler.class);

    public TCPClientHandler(Torrent torrent, DownloadManager downloadManager) {
        super();
        this.torrent = torrent;
        this.downloadManager = downloadManager;
    }

    @Override
    void doChock(ChannelHandlerContext ctx) {
        closePeer(ctx);
    }

    @Override
    void doUnChock(ChannelHandlerContext ctx, ByteBuf data) {
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
                    if (downloadConfig.isShowDownloadLog()) {
                        logger.info("Client: request {}, torrent name: {}, index: {}, begin: {}, length: {}, piece queue size: {}",
                                ctx.channel().remoteAddress(), torrent.getName(),
                                requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength(),
                                pieceQueue.size());
                    }
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
    void doBitField(ChannelHandlerContext ctx, ByteBuf data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        // 根据peer返回的区块完成信息生成区块下载队列
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig != null && downloadConfig.getPieceQueue() == null) {
            downloadConfig.setOnePieceRequestSize(
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
    void doPiece(ChannelHandlerContext ctx, ByteBuf data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        PeerWirePayload peerWirePayload = (PeerWirePayload) peerWire.getPayload();
        if (downloadManager.saveBytesToFile(peerWirePayload)) {
            logger.info("Client: download {} complete!", torrent.getName());
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
}

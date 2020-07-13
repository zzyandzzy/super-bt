package xyz.zzyitj.nbt.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
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

    public TCPClientHandler(Torrent torrent, AbstractDownloadManager downloadManager) {
        super();
        this.torrent = torrent;
        this.downloadManager = downloadManager;
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
        if (torrent == null) {
            return;
        }
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig == null) {
            return;
        }
        Queue<RequestPiece> pieceQueue = downloadConfig.getPieceQueue();
        if (pieceQueue == null) {
            return;
        }
        RequestPiece requestPiece = pieceQueue.poll();
        // 下载队列里面已经完成了
        if (requestPiece == null) {
            // 但是还有些失败的区块
            boolean[] pieceRequestProcess = downloadConfig.getPieceRequestProcess();
            for (int i = 0; i < pieceRequestProcess.length; i++) {
                if (!pieceRequestProcess[i]) {
                    // 构造区块
                    int onePieceRequestSize = downloadConfig.getOnePieceRequestSize();
                    int index = i / onePieceRequestSize;
                    int begin = i % onePieceRequestSize * HandshakeUtils.PIECE_MAX_LENGTH;
                    // 如果不是最后一个区块，则大小都是16384
                    if (i != pieceRequestProcess.length - 1) {
                        requestPiece = new RequestPiece(index, begin, HandshakeUtils.PIECE_MAX_LENGTH);
                    } else {
                        // 最后一个区块，大小是种子大小减去前面区块的总大小
                        long beforePieceRequestSize = i * HandshakeUtils.PIECE_MAX_LENGTH;
                        requestPiece = new RequestPiece(index, begin, (int) (torrent.getTorrentLength() - beforePieceRequestSize));
                    }
                    logger.info("reply download: {}, onePieceRequestSize: {}, {}", i, onePieceRequestSize, requestPiece);
                    break;
                }
            }
        }
        if (requestPiece != null) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(
                    HandshakeUtils.requestPieceHandler(requestPiece)));
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: request {}, torrent name: {}, index: {}, begin: {}, length: {}, piece queue size: {}",
                        ctx.channel().remoteAddress(), torrent.getName(),
                        requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength(), pieceQueue.size());
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
            HandshakeUtils.generateRequestPieceQueue(peerWire, torrent, downloadConfig);
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
        if (downloadManager.save(peerWirePayload)) {
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

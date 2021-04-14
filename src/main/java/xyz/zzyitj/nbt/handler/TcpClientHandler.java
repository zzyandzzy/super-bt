package xyz.zzyitj.nbt.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Configuration;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.HandlerUtils;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 6:58 下午
 * @since 1.0
 */
public class TcpClientHandler extends AbstractTcpHandler {
    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    public TcpClientHandler(Torrent torrent, AbstractDownloadManager downloadManager) {
        super();
        this.torrent = torrent;
        this.downloadManager = downloadManager;
    }

    @Override
    void doChock(ChannelHandlerContext ctx) {
        if (HandlerUtils.closePeer(ctx)) {
            unChoke = false;
        }
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
        DownloadConfig downloadConfig = Configuration.downloadConfigMap.get(torrent);
        if (downloadConfig == null) {
            return;
        }
        Map<Integer, RequestPiece> pieceRequestMap = downloadConfig.getPieceRequestMap();
        if (pieceRequestMap == null) {
            return;
        }
        AtomicInteger atomicPieceRequestMapKey = downloadConfig.getPieceRequestMapKey();
        int pieceRequestMapKey = atomicPieceRequestMapKey.getAndIncrement();
        Queue<RequestPiece> failPieceRequestQueue = downloadConfig.getFailPieceRequest();
        // 说明了已经请求下载map里面全部都发送了请求，但是可能还没下载完，因为可能有失败的
        if (pieceRequestMapKey == downloadConfig.getPieceRequestMapSize() ||
                downloadConfig.isFailDownloadPieceRequest() && failPieceRequestQueue.size() == 0) {
            if (pieceRequestMap.isEmpty()) {
                return;
            }
            downloadConfig.setFailDownloadPieceRequest(true);
            atomicPieceRequestMapKey.set(downloadConfig.getPieceRequestMapSize() - 1);
            try {
                Thread.sleep(Const.TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (this) {
                if (failPieceRequestQueue.size() == 0) {
                    for (Integer integer : pieceRequestMap.keySet()) {
                        failPieceRequestQueue.offer(pieceRequestMap.get(integer));
                    }
                }
            }
        }
        RequestPiece requestPiece;
        if (failPieceRequestQueue.size() == 0) {
            requestPiece = pieceRequestMap.get(pieceRequestMapKey);
        } else {
            requestPiece = failPieceRequestQueue.poll();
        }
        // 下载队列里面已经完成了
        if (requestPiece == null) {
            return;
        }
        ctx.writeAndFlush(Unpooled.copiedBuffer(
                HandshakeUtils.requestPieceHandler(requestPiece)));
        if (downloadConfig.isShowRequestLog()) {
            logger.info("Client: request {}, torrent name: {}, index: {}, begin: {}, length: {}," +
                            " piece map size: {}, map key: {}, fail piece size: {}",
                    ctx.channel().remoteAddress(), torrent.getName(),
                    requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength(),
                    pieceRequestMap.size(), pieceRequestMapKey, failPieceRequestQueue.size());
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
        DownloadConfig downloadConfig = Configuration.downloadConfigMap.get(torrent);
        if (downloadConfig != null && downloadConfig.getPieceRequestMap() == null) {
            HandshakeUtils.generatePieceRequestMap(peerWire, torrent, downloadConfig);
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
            // 关闭所有peer
            DownloadConfig downloadConfig = Configuration.downloadConfigMap.get(torrent);
            if (downloadConfig != null && !downloadConfig.isCloseAllPeer()) {
                downloadConfig.setCloseAllPeer(true);
                HandlerUtils.closeAllPeer(torrent);
                unChoke = false;
            }
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

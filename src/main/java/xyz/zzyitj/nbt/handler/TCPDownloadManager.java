package xyz.zzyitj.nbt.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.util.DownloadManagerUtils;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:55 下午
 * @since 1.0
 */
public class TCPDownloadManager implements DownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(TCPDownloadManager.class);
    private Torrent torrent;
    /**
     * 记录的是下载当前已下载的大小
     */
    private final AtomicLong downloadSum = new AtomicLong(0);

    @Override
    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    @Override
    public boolean saveBytesToFile(PeerWirePayload payload) {
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig != null) {
            Queue<RequestPiece> pieceQueue = downloadConfig.getPieceQueue();
            if (pieceQueue != null) {
                if (torrent.getTorrentFileItemList() == null) {
                    return saveSingleFile(payload, downloadConfig, pieceQueue);
                } else {
                    return saveMultipleFile(payload, downloadConfig, pieceQueue);
                }
            }
        }
        return false;
    }

    /**
     * 下载多个文件
     *
     * @param peerWirePayload 数据域
     * @param downloadConfig  下载配置信息
     * @param pieceQueue      下载队列
     * @return true下载完成，false下载未完成
     */
    private boolean saveMultipleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig,
                                     Queue<RequestPiece> pieceQueue) {
        // 跳过的字节数
        long skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        // 根据跳过的字节数确定当前写入的文件的下标
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
        // 当前写入的文件
        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
        byte[] block = null;
        RandomAccessFile randomAccessFile = null;
        File file = new File(downloadConfig.getSavePath() + torrent.getName() +
                File.separator + torrentFileItem.getPath());
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            float currentDownload;
            // 大于0表示当前文件加上现在的字节还有超出
            // 小于0表示当前文件加上现在的字节还没下载完
            // 等于0恰好把当前文件加上现在的字节刚好下载完
            // 超出的字节数
            long bytesExceeded = (randomAccessFile.length() + peerWirePayload.getBlock().readableBytes()) - torrentFileItem.getLength();
            if (bytesExceeded > 0) {
                block = new byte[peerWirePayload.getBlock().readableBytes() - (int) (bytesExceeded)];
            } else {
                block = new byte[peerWirePayload.getBlock().readableBytes()];
            }
            peerWirePayload.getBlock().readBytes(block);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, piece queue size: {}, download sum: {}",
                        torrent.getName(), torrent.getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), pieceQueue.size(), downloadSum);
            }
            // 跳过多少字节
            randomAccessFile.seek(DownloadManagerUtils.getStartPosition(skipBytes, torrent));
            // 写入文件
            randomAccessFile.write(block);
            // 下载进度
            currentDownload = downloadSum.addAndGet(block.length);
            if (downloadConfig.isShowDownloadProcess()) {
                logger.info("torrent name: {}, downloading progress: {}%",
                        torrent.getName(),
                        (currentDownload / torrent.getTorrentLength()) * 100);
            }
            // 可能恰好下载完
            if (checkComplete(pieceQueue, torrent)) {
                return true;
            }
            // 然后递归写入未完成的部分
            if (bytesExceeded > 0) {
                peerWirePayload.setBegin(peerWirePayload.getBegin() + block.length);
                return saveMultipleFile(peerWirePayload, downloadConfig, pieceQueue);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            int blockLength = block == null ? peerWirePayload.getBlock().readableBytes() : block.length;
            logger.error("Client: error, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, piece queue size: {}, download sum: {}",
                    torrent.getName(), torrent.getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), blockLength, file.length(),
                    skipBytes, pieceQueue.size(), downloadSum);
            if (blockLength != 0) {
                pieceQueue.offer(new RequestPiece(
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), blockLength));
            }
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (peerWirePayload.getBlock() != null) {
                peerWirePayload.getBlock().release();
            }
        }
        return false;
    }

    /**
     * 检查是否下载完全部文件
     *
     * @param pieceQueue 下载队列
     * @return true下载完成，false下载未完成
     */
    private boolean checkComplete(Queue<RequestPiece> pieceQueue, Torrent torrent) {
        return pieceQueue.size() == 0 && downloadSum.get() >= torrent.getTorrentLength();
    }

    /**
     * 下载单个文件
     *
     * @param peerWirePayload 数据域
     * @param downloadConfig  下载信息
     * @param pieceQueue      下载队列
     * @return true，下载完成，false没有下载完
     */
    private boolean saveSingleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig,
                                   Queue<RequestPiece> pieceQueue) {
        // 跳过多少字节
        // index * onePieceRequestSum * 16Kb + begin
        long skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        File file = new File(downloadConfig.getSavePath() + torrent.getName());
        RandomAccessFile randomAccessFile = null;
        byte[] block = new byte[peerWirePayload.getBlock().readableBytes()];
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            peerWirePayload.getBlock().readBytes(block);
            randomAccessFile.seek(skipBytes);
            randomAccessFile.write(block);
            float currentDownload = downloadSum.addAndGet(block.length);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, " +
                                "piece queue size: {}",
                        torrent.getName(), peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), torrent.getTorrentLength(),
                        pieceQueue.size());
            }
            if (downloadConfig.isShowDownloadProcess()) {
                logger.info("torrent name: {}, downloading progress: {}%",
                        torrent.getName(),
                        (currentDownload / torrent.getTorrentLength()) * 100);
            }
            // 判断是否要继续下载区块
            if (checkComplete(pieceQueue, torrent)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, piece queue size: {}",
                    torrent.getName(), torrent.getTorrentLength(),
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length, file.length(),
                    skipBytes, pieceQueue.size());
            if (block.length != 0) {
                pieceQueue.offer(new RequestPiece(
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length));
            }
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (peerWirePayload.getBlock() != null) {
                peerWirePayload.getBlock().release();
            }
        }
        return false;
    }
}

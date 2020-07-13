package xyz.zzyitj.nbt.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.util.DownloadManagerUtils;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:55 下午
 * @since 1.0
 */
public class TCPDownloadManager extends AbstractDownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(TCPDownloadManager.class);

    @Override
    public boolean save(PeerWirePayload payload) {
        if (getTorrent() == null) {
            return false;
        }
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(getTorrent());
        if (downloadConfig == null) {
            return false;
        }
        Map<Integer, RequestPiece> pieceMap = downloadConfig.getPieceMap();
        if (pieceMap == null) {
            return false;
        }
        if (getTorrent().getTorrentFileItemList() == null) {
            return saveSingleFile(payload, downloadConfig, pieceMap);
        } else {
            return saveMultipleFile(payload, downloadConfig, pieceMap);
        }
    }

    /**
     * 下载多个文件
     *
     * @param peerWirePayload 数据域
     * @param downloadConfig  下载配置信息
     * @param pieceMap        下载map
     * @return true下载完成，false下载未完成
     */
    private boolean saveMultipleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig,
                                     Map<Integer, RequestPiece> pieceMap) {
        // 跳过的字节数
        long skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        // 根据跳过的字节数确定当前写入的文件的下标
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, getTorrent());
        // 当前写入的文件
        TorrentFileItem torrentFileItem = getTorrent().getTorrentFileItemList().get(fileIndex);
        byte[] block = null;
        long bytesExceeded = 0;
        AtomicInteger atomicDownloadIndex = downloadConfig.getDownloadIndex();
        int downloadIndex = atomicDownloadIndex.get();
        RandomAccessFile randomAccessFile = null;
        File file = new File(downloadConfig.getSavePath() + getTorrent().getName() +
                File.separator + torrentFileItem.getPath());
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // 大于0表示当前文件加上现在的字节还有超出
            // 小于0表示当前文件加上现在的字节还没下载完
            // 等于0恰好把当前文件加上现在的字节刚好下载完
            // 超出的字节数
            bytesExceeded = (randomAccessFile.length() + peerWirePayload.getBlock().readableBytes()) - torrentFileItem.getLength();
            if (bytesExceeded > 0) {
                block = new byte[peerWirePayload.getBlock().readableBytes() - (int) (bytesExceeded)];
            } else {
                block = new byte[peerWirePayload.getBlock().readableBytes()];
            }
            peerWirePayload.getBlock().readBytes(block);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, download index: {}, piece map size: {}",
                        getTorrent().getName(), getTorrent().getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), downloadIndex, pieceMap.size());
            }
            // 跳过多少字节
            randomAccessFile.seek(DownloadManagerUtils.getStartPosition(skipBytes, getTorrent()));
            // 写入文件
            randomAccessFile.write(block);
            if (downloadIndex < pieceMap.size()) {
                pieceMap.get(downloadIndex).setDownload(true);
            }
            atomicDownloadIndex.incrementAndGet();
            // 可能恰好下载完
            if (checkComplete(pieceMap, downloadIndex)) {
                return true;
            }
            // 然后递归写入未完成的部分
            if (bytesExceeded > 0) {
                peerWirePayload.setBegin(peerWirePayload.getBegin() + block.length);
                return saveMultipleFile(peerWirePayload, downloadConfig, pieceMap);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            int blockLength = block == null ? peerWirePayload.getBlock().readableBytes() : block.length;
            logger.error("Client: error, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, download index: {}, piece map size: {}",
                    getTorrent().getName(), getTorrent().getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), blockLength, file.length(),
                    skipBytes, downloadIndex, pieceMap.size());
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (peerWirePayload.getBlock() != null && bytesExceeded == 0) {
                peerWirePayload.getBlock().release();
            }
        }
        return false;
    }

    /**
     * 检查是否下载完全部文件
     *
     * @param pieceMap 下载map
     * @return true下载完成，false下载未完成
     */
    private boolean checkComplete(Map<Integer, RequestPiece> pieceMap, int downloadIndex) {
        if (downloadIndex >= pieceMap.size()) {
            for (RequestPiece piece : pieceMap.values()) {
                if (!piece.isDownload()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 下载单个文件
     *
     * @param peerWirePayload 数据域
     * @param downloadConfig  下载信息
     * @param pieceMap        下载map
     * @return true，下载完成，false没有下载完
     */
    private boolean saveSingleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig,
                                   Map<Integer, RequestPiece> pieceMap) {
        // 跳过多少字节
        // index * onePieceRequestSum * 16Kb + begin
        long skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        File file = new File(downloadConfig.getSavePath() + getTorrent().getName());
        RandomAccessFile randomAccessFile = null;
        byte[] block = new byte[peerWirePayload.getBlock().readableBytes()];
        AtomicInteger atomicDownloadIndex = downloadConfig.getDownloadIndex();
        int downloadIndex = atomicDownloadIndex.get();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            peerWirePayload.getBlock().readBytes(block);
            randomAccessFile.seek(skipBytes);
            randomAccessFile.write(block);
            if (downloadIndex < pieceMap.size()) {
                pieceMap.get(downloadIndex).setDownload(true);
            }
            atomicDownloadIndex.incrementAndGet();
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, " +
                                "download index: {}, piece map size: {}",
                        getTorrent().getName(), peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), getTorrent().getTorrentLength(),
                        downloadIndex, pieceMap);
            }
            // 判断是否要继续下载区块
            if (checkComplete(pieceMap, downloadIndex)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, download index: {}, piece map size: {}",
                    getTorrent().getName(), getTorrent().getTorrentLength(),
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length, file.length(),
                    skipBytes, downloadIndex, pieceMap.size());
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

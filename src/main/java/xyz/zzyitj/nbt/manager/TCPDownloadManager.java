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
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

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
        Queue<RequestPiece> pieceQueue = downloadConfig.getPieceQueue();
        if (pieceQueue == null) {
            return false;
        }
        if (getTorrent().getTorrentFileItemList() == null) {
            return saveSingleFile(payload, downloadConfig, pieceQueue);
        } else {
            return saveMultipleFile(payload, downloadConfig, pieceQueue);
        }
    }

    /**
     * 下载多个文件
     *
     * @param payload        数据域
     * @param downloadConfig 下载配置信息
     * @param pieceQueue     下载队列
     * @return true下载完成，false下载未完成
     */
    private boolean saveMultipleFile(PeerWirePayload payload, DownloadConfig downloadConfig,
                                     Queue<RequestPiece> pieceQueue) {
        // 跳过的字节数
        long skipBytes = payload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + payload.getBegin();
        // 根据跳过的字节数确定当前写入的文件的下标
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, getTorrent());
        // 当前写入的文件
        TorrentFileItem torrentFileItem = getTorrent().getTorrentFileItemList().get(fileIndex);
        byte[] block = payload.getBlock();
        byte[] writeBlock = null;
        long bytesExceeded;
        RandomAccessFile randomAccessFile = null;
        File file = new File(downloadConfig.getSavePath() + getTorrent().getName() +
                File.separator + torrentFileItem.getPath());
        AtomicLong atomicDownloadSum = downloadConfig.getDownloadSum();
        long downloadSum = atomicDownloadSum.get();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // 大于0表示当前文件加上现在的字节还有超出
            // 小于0表示当前文件加上现在的字节还没下载完
            // 等于0恰好把当前文件加上现在的字节刚好下载完
            // 超出的字节数
            bytesExceeded = (randomAccessFile.length() + block.length) - torrentFileItem.getLength();
            if (bytesExceeded > 0) {
                writeBlock = new byte[block.length - (int) (bytesExceeded)];
                System.arraycopy(block, 0, writeBlock, 0, writeBlock.length);
            } else {
                writeBlock = block;
            }
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                                "index: {}, begin: {}, block length: {}, write block length: {}, " +
                                "skip bytes: {}, random access file length: {}, download sum: {}, piece map size: {}",
                        getTorrent().getName(), getTorrent().getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                        payload.getIndex(), payload.getBegin(), block.length, writeBlock.length,
                        skipBytes, randomAccessFile.length(), downloadSum, pieceQueue.size());
            }
            // 跳过多少字节
            randomAccessFile.seek(DownloadManagerUtils.getStartPosition(skipBytes, getTorrent()));
            // 写入文件
            randomAccessFile.write(writeBlock);
            // 可能恰好下载完
            if (checkComplete(pieceQueue, atomicDownloadSum.addAndGet(writeBlock.length), getTorrent())) {
                return true;
            }
            // 然后递归写入未完成的部分
            if (bytesExceeded > 0) {
                payload.setBegin(payload.getBegin() + writeBlock.length);
                byte[] newBlock = new byte[block.length - writeBlock.length];
                System.arraycopy(block, writeBlock.length, newBlock, 0, newBlock.length);
                payload.setBlock(newBlock);
                return saveMultipleFile(payload, downloadConfig, pieceQueue);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, file name: {}, file length: {}, " +
                            "index: {}, begin: {}, block length: {}, write block length: {}, file length: {}, " +
                            "skip bytes: {}, download sum: {}, piece queue size: {}",
                    getTorrent().getName(), getTorrent().getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                    payload.getIndex(), payload.getBegin(), block.length, writeBlock, file.length(),
                    skipBytes, downloadSum, pieceQueue.size());
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    private boolean checkComplete(Queue<RequestPiece> pieceQueue, long downloadSum, Torrent torrent) {
        return pieceQueue.size() == 0 && downloadSum == torrent.getTorrentLength();
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
        File file = new File(downloadConfig.getSavePath() + getTorrent().getName());
        RandomAccessFile randomAccessFile = null;
        AtomicLong atomicDownloadSum = downloadConfig.getDownloadSum();
        long downloadSum = atomicDownloadSum.get();
        byte[] block = peerWirePayload.getBlock();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(skipBytes);
            randomAccessFile.write(block);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, download sum: {}" +
                                "piece queue size: {}",
                        getTorrent().getName(), getTorrent().getTorrentLength(),
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), downloadSum,
                        pieceQueue.size());
            }
            // 判断是否要继续下载区块
            if (checkComplete(pieceQueue, atomicDownloadSum.addAndGet(block.length), getTorrent())) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, download sum: {}, piece map size: {}",
                    getTorrent().getName(), getTorrent().getTorrentLength(),
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length, file.length(),
                    skipBytes, downloadSum, pieceQueue.size());
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}

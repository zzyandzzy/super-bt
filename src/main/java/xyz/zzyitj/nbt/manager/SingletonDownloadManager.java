package xyz.zzyitj.nbt.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.util.DownloadManagerUtils;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * xyz.zzyitj.nbt.manager
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/13 5:53 下午
 * @since 1.0
 */
public class SingletonDownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(SingletonDownloadManager.class);
    private static volatile SingletonDownloadManager INSTANCE;

    public SingletonDownloadManager() {
    }

    public static SingletonDownloadManager getInstance() {
        if (INSTANCE == null) {
            synchronized (SingletonDownloadManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SingletonDownloadManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 下载多个文件
     *
     * @param torrent        种子
     * @param payload        数据域
     * @param downloadConfig 下载配置信息
     * @param pieceQueue     下载队列
     * @return true下载完成，false下载未完成
     */
    public boolean saveMultipleFile(Torrent torrent, PeerWirePayload payload, DownloadConfig downloadConfig,
                                    Queue<RequestPiece> pieceQueue) {
        // 跳过的字节数
        long skipBytes = payload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + payload.getBegin();
        // 根据跳过的字节数确定当前写入的文件的下标
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
        // 当前写入的文件
        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
        byte[] block = payload.getBlock();
        byte[] writeBlock = null;
        long bytesExceeded = block.length;
        RandomAccessFile randomAccessFile = null;
        AtomicLong atomicDownloadSum = downloadConfig.getDownloadSum();
        long downloadSum = atomicDownloadSum.get();
        try {
            long startPosition = DownloadManagerUtils.getStartPosition(skipBytes, torrent);
            // 大于0表示当前文件加上现在的字节还有超出
            // 小于0表示当前文件加上现在的字节还没下载完
            // 等于0恰好把当前文件加上现在的字节刚好下载完
            // 超出的字节数
            bytesExceeded = startPosition + block.length - torrentFileItem.getLength();
            if (bytesExceeded > 0) {
                writeBlock = new byte[block.length - (int) (bytesExceeded)];
                System.arraycopy(block, 0, writeBlock, 0, writeBlock.length);
            } else {
                writeBlock = block;
            }
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, item file name: {}, item file length: {}, " +
                                "index: {}, begin: {}, block length: {}, write block length: {}, " +
                                "skip bytes: {}, download sum: {}, bytes exceeded: {}, piece map size: {}",
                        torrent.getName(), torrent.getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                        payload.getIndex(), payload.getBegin(), block.length, writeBlock.length,
                        skipBytes, downloadSum, bytesExceeded, pieceQueue.size());
            }
            File file = new File(downloadConfig.getSavePath() + torrent.getName() +
                    File.separator + torrentFileItem.getPath());
            randomAccessFile = new RandomAccessFile(file, "rw");
            // 跳过多少字节
            randomAccessFile.seek(startPosition);
            // 写入文件
            randomAccessFile.write(writeBlock);
            // 可能恰好下载完
            if (checkDownloadComplete(pieceQueue, atomicDownloadSum.addAndGet(writeBlock.length), torrent)) {
                return true;
            }
            // 然后递归写入未完成的部分
            if (bytesExceeded > 0) {
                payload.setBegin(payload.getBegin() + writeBlock.length);
                byte[] newBlock = new byte[block.length - writeBlock.length];
                System.arraycopy(block, writeBlock.length, newBlock, 0, newBlock.length);
                payload.setBlock(newBlock);
                return saveMultipleFile(torrent, payload, downloadConfig, pieceQueue);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, item file name: {}, item file length: {}, " +
                            "index: {}, begin: {}, block length: {}, write block length: {}, " +
                            "skip bytes: {}, download sum: {}, bytes exceeded: {}, piece queue size: {}",
                    torrent.getName(), torrent.getTorrentLength(), torrentFileItem.getPath(), torrentFileItem.getLength(),
                    payload.getIndex(), payload.getBegin(), block.length, writeBlock,
                    skipBytes, downloadSum, bytesExceeded, pieceQueue.size());
            pieceQueue.offer(new RequestPiece(payload.getIndex(), payload.getBegin(), block.length));
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
     * 下载单个文件
     *
     * @param torrent        种子
     * @param payload        数据域
     * @param downloadConfig 下载信息
     * @param pieceQueue     下载队列
     * @return true，下载完成，false没有下载完
     */
    public boolean saveSingleFile(Torrent torrent, PeerWirePayload payload, DownloadConfig downloadConfig,
                                  Queue<RequestPiece> pieceQueue) {
        // 跳过多少字节
        // index * onePieceRequestSum * 16Kb + begin
        long skipBytes = payload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + payload.getBegin();
        File file = new File(downloadConfig.getSavePath() + torrent.getName());
        RandomAccessFile randomAccessFile = null;
        AtomicLong atomicDownloadSum = downloadConfig.getDownloadSum();
        long downloadSum = atomicDownloadSum.get();
        byte[] block = payload.getBlock();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(skipBytes);
            randomAccessFile.write(block);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, torrent length: {}, " +
                                "index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, download sum: {}" +
                                "piece queue size: {}",
                        torrent.getName(), torrent.getTorrentLength(),
                        payload.getIndex(), payload.getBegin(), block.length,
                        skipBytes, randomAccessFile.length(), downloadSum,
                        pieceQueue.size());
            }
            // 判断是否要继续下载区块
            if (checkDownloadComplete(pieceQueue, atomicDownloadSum.addAndGet(block.length), torrent)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Client: error, torrent name: {}, torrent length: {}, " +
                            "index: {}, begin: {}, block length: {}, file length: {}, " +
                            "skip bytes: {}, download sum: {}, piece map size: {}",
                    torrent.getName(), torrent.getTorrentLength(),
                    payload.getIndex(), payload.getBegin(), block.length, file.length(),
                    skipBytes, downloadSum, pieceQueue.size());
            pieceQueue.offer(new RequestPiece(payload.getIndex(), payload.getBegin(), block.length));
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
    private static boolean checkDownloadComplete(Queue<RequestPiece> pieceQueue, long downloadSum, Torrent torrent) {
        return pieceQueue.size() == 0 && downloadSum == torrent.getTorrentLength();
    }

}

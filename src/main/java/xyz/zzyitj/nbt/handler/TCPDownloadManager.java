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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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
     * 记录了随机存储文件的文件打开描述符
     */
    private final Map<Integer, RandomAccessFile> randomAccessFileMap = new ConcurrentHashMap<>();
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
                }
//                else {
//                    return saveMultipleFile(payload, downloadConfig, pieceQueue);
//                }
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
//    private boolean saveMultipleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig,
//                                     Queue<RequestPiece> pieceQueue) {
//        byte[] block = peerWirePayload.getBlock();
//        int skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
//                + peerWirePayload.getBegin();
//        // 1.判断该区块的位置是属于哪个文件
//        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
//        RandomAccessFile randomAccessFile = randomAccessFileMap.get(fileIndex);
//        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
//        long newLength = 0;
//        try {
//            if (randomAccessFile == null) {
//                File file = new File(downloadConfig.getSavePath() + torrent.getName() +
//                        File.separator + torrentFileItem.getPath());
//                randomAccessFile = new RandomAccessFile(file, "rw");
//            }
//            // 2.判断peer发送过来的字节是否超出该文件
//            // 大于0，说明发送过来的字节超出了文件
//            // 小于零，说明当前文件的字节可能还没传输完
//            newLength = (block.length + randomAccessFile.length()) - torrentFileItem.getLength();
//            float currentDownload = 0;
//            if (newLength >= 0) {
//                // 当前写入的字节长度
//                int currentBlockLength = (int) (Math.abs(block.length - newLength));
//                // 正好是最后一个区块
//                if (randomAccessFile.length() >= torrentFileItem.getLength()) {
//                    currentBlockLength = (int) Math.min(newLength, HandshakeUtils.PIECE_MAX_LENGTH);
//                    logger.info("blockLength: {}, currentBlockLength: {}, randomAccessFileLength: {}, " +
//                                    "torrentFileItemLength: {}, newLength: {}, torrentLength: {}, downloadSum: {}",
//                            block.length, currentBlockLength, randomAccessFile.length(),
//                            torrentFileItem.getLength(), newLength, torrent.getTorrentLength(), downloadSum.get());
//                }
//                // 先写入文件
//                randomAccessFile.seek(DownloadManagerUtils.getStartPosition(skipBytes, torrent));
//                randomAccessFile.write(block, 0, currentBlockLength);
//                currentDownload = downloadSum.addAndGet(currentBlockLength);
//                logger.info("downloading progress: {}%", (currentDownload / torrent.getTorrentLength()) * 100);
//                // 因为大于等于0说明发送过来的字节超出了文件，所以当前的文件一定是下载完毕了的，自己关闭IO流
//                randomAccessFile.close();
//                // 检查是否下载完毕全部文件
//                if (newLength == 0 || newLength == block.length && checkComplete(pieceQueue, torrent)) {
//                    return true;
//                }
//                PeerWirePayload payload = new PeerWirePayload();
//                payload.setIndex(peerWirePayload.getIndex());
//                payload.setBegin(peerWirePayload.getBegin() + currentBlockLength);
//                byte[] newBlock = new byte[(int) newLength];
//                System.arraycopy(block, currentBlockLength, newBlock, 0, (int) newLength);
//                payload.setBlock(newBlock);
//                // 再分割数组
//                return saveMultipleFile(payload, downloadConfig, pieceQueue);
//            } else {
//                // 写入文件
//                randomAccessFile.seek(DownloadManagerUtils.getStartPosition(skipBytes, torrent));
//                randomAccessFile.write(block);
//                currentDownload = downloadSum.addAndGet(block.length);
//                logger.info("downloading progress: {}%%", (currentDownload / torrent.getTorrentLength()) * 100);
//                // 可能恰好下载完
//                if (checkComplete(pieceQueue, torrent)) {
//                    randomAccessFile.close();
//                    return true;
//                }
//                return false;
//            }
//        } catch (Exception e) {
//            logger.error("Client: response piece, index: {}, begin: {}, blockLength: {}, " +
//                            "skipBytes: {}, fileName: {}, fileLength: {}, " +
//                            " torrentLength: {}, newLength: {}, pieceQueueSize: {}",
//                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), block.length,
//                    skipBytes, torrentFileItem.getPath(), torrentFileItem.getLength(),
//                    torrent.getTorrentLength(), newLength, pieceQueue.size());
//            pieceQueue.offer(new RequestPiece(
//                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().readableBytes()));
//            e.printStackTrace();
//        }
//        return false;
//    }

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
        int skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSize() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
        try {
            byte[] block = new byte[peerWirePayload.getBlock().readableBytes()];
            peerWirePayload.getBlock().readBytes(block);
            RandomAccessFile randomAccessFile = randomAccessFileMap.get(fileIndex);
            if (randomAccessFile == null) {
                File file = new File(downloadConfig.getSavePath() + torrent.getName());
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFileMap.put(fileIndex, randomAccessFile);
            }
            randomAccessFile.seek(skipBytes);
            randomAccessFile.write(block);
            float currentDownload = downloadSum.addAndGet(block.length);
            if (downloadConfig.isShowDownloadLog()) {
                logger.info("Client: response piece, torrent name: {}, index: {}, begin: {}, block length: {}, " +
                                "skip bytes: {}, random access file length: {}, torrent length: {}, " +
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
                randomAccessFile.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            pieceQueue.offer(new RequestPiece(
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().readableBytes()));
        }
        return false;
    }
}

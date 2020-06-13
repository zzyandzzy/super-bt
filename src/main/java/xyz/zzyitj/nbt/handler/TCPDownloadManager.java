package xyz.zzyitj.nbt.handler;

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

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:55 下午
 * @since 1.0
 */
public class TCPDownloadManager implements DownloadManager {
    private Torrent torrent;
    /**
     * 记录了随机存储文件的文件打开描述符
     */
    private final Map<Integer, RandomAccessFile> randomAccessFileMap = new ConcurrentHashMap<>();
    /**
     * 记录了种子文件写入的字节数，判断是否结束
     */
    private final Map<Torrent, Long> writeLengthMap = new ConcurrentHashMap<>();
    /**
     * 记录失败次数
     */
    private final Map<Integer, Integer> failDownloadMap = new ConcurrentHashMap<>();
    /**
     * 5次下载该区块都失败后不再尝试
     */
    private static final int MAX_FAIL_DOWNLOAD_COUNT = 5;

    @Override
    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    @Override
    public boolean saveBytesToFile(PeerWirePayload payload) {
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (downloadConfig != null) {
            Queue<RequestPiece> queue = downloadConfig.getQueue();
            if (queue != null) {
                if (torrent.getTorrentFileItemList() == null) {
                    return saveSingleFile(payload, downloadConfig, queue);
                } else {
                    return saveMultipleFile(payload, downloadConfig, queue);
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
     * @param queue           下载队列
     * @return
     */
    private boolean saveMultipleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig, Queue<RequestPiece> queue) {
        byte[] block = peerWirePayload.getBlock();
        int skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSum() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        // 1.判断该区块的位置是属于哪个文件
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
        RandomAccessFile randomAccessFile = randomAccessFileMap.get(fileIndex);
        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
        try {
            if (randomAccessFile == null) {
                File file = new File(downloadConfig.getSavePath() + torrent.getName() +
                        File.separator + torrentFileItem.getPath());
                randomAccessFile = new RandomAccessFile(file, "rw");
            }
            // 2.判断peer发送过来的字节是否超出该文件
            // 大于0，说明发送过来的字节超出了文件
            // 小于零，说明当前文件的字节可能还没传输完
            long newLength = (block.length + randomAccessFile.length()) - torrentFileItem.getLength();
            System.out.printf("Client: response piece, index: %d, begin: %d, length: %d, " +
                            "skipBytes: %d, file: %s, fileLength: %d, torrentLength: %d, newLength: %d, need: %d\n",
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().length,
                    skipBytes, torrentFileItem.getPath(), randomAccessFile.length(), torrent.getTorrentLength(), newLength, queue.size());
            if (newLength >= 0) {
                // 当前写入的长度
                int blockLength = (int) (block.length - newLength);
                // 先写入文件
                randomAccessFile.skipBytes(skipBytes);
                randomAccessFile.write(block, 0, blockLength);
                randomAccessFile.close();
                System.out.printf("Client: download %s complete!\n", torrentFileItem.getPath());
                if (checkComplete(queue, torrentFileItem, newLength)) {
                    return true;
                }
                PeerWirePayload payload = new PeerWirePayload();
                payload.setIndex(peerWirePayload.getIndex());
                payload.setBegin(peerWirePayload.getBegin() + blockLength);
                byte[] newBlock = new byte[(int) newLength];
                System.arraycopy(block, blockLength, newBlock, 0, (int) newLength);
                payload.setBlock(newBlock);
                // 再分割数组
                return saveMultipleFile(payload, downloadConfig, queue);
            } else {
                // 写入文件
                randomAccessFile.skipBytes(skipBytes);
                randomAccessFile.write(block);
                if (checkComplete(queue, torrentFileItem, newLength)) {
                    randomAccessFile.close();
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查是否完成
     *
     * @param queue           下载队列
     * @param torrentFileItem 文件
     * @return true下载完成，false下载未完成
     */
    private boolean checkComplete(Queue<RequestPiece> queue, TorrentFileItem torrentFileItem, long newLength) {
        if (newLength == 0 && queue.size() == 0) {
            return true;
        }
        Long writeLength = writeLengthMap.get(torrent);
        if (writeLength == null) {
            writeLength = torrentFileItem.getLength();
        } else {
            writeLength += torrentFileItem.getLength();
        }
        writeLengthMap.put(torrent, writeLength);
        // 判断是否下载完
        return queue.size() == 0 && writeLength == torrent.getTorrentLength();
    }

    /**
     * 下载单个文件
     *
     * @param peerWirePayload 数据域
     * @param downloadConfig  下载信息
     * @param queue           下载队列
     * @return true，下载完成，false没有下载完
     */
    private boolean saveSingleFile(PeerWirePayload peerWirePayload, DownloadConfig downloadConfig, Queue<RequestPiece> queue) {
        // 跳过多少字节
        // index * onePieceRequestSum * 16Kb + begin
        int skipBytes = peerWirePayload.getIndex() * downloadConfig.getOnePieceRequestSum() * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
        try {
            RandomAccessFile randomAccessFile = randomAccessFileMap.get(fileIndex);
            if (randomAccessFile == null) {
                File file = new File(downloadConfig.getSavePath() + torrent.getName());
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFileMap.put(fileIndex, randomAccessFile);
            }
            randomAccessFile.skipBytes(skipBytes);
            randomAccessFile.write(peerWirePayload.getBlock());
            System.out.printf("Client: response piece, index: %d, begin: %d, length: %d, " +
                            "skipBytes: %d, fileLength: %d, torrentLength: %d, need: %d\n",
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().length,
                    skipBytes, randomAccessFile.length(), torrent.getTorrentLength(), queue.size());
            // 判断是否要继续下载区块
            if (queue.size() == 0 && randomAccessFile.length() == torrent.getTorrentLength()) {
                randomAccessFile.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Integer failCount = failDownloadMap.get(fileIndex);
            if (failCount == null) {
                failDownloadMap.put(fileIndex, 1);
                queue.offer(new RequestPiece(
                        peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().length));
            } else {
                if (failCount < MAX_FAIL_DOWNLOAD_COUNT) {
                    failDownloadMap.put(fileIndex, failCount + 1);
                    queue.offer(new RequestPiece(
                            peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().length));
                }
            }
        }
        return false;
    }
}

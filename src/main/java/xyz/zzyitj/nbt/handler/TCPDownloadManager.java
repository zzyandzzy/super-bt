package xyz.zzyitj.nbt.handler;

import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.RequestPiece;
import xyz.zzyitj.nbt.bean.Torrent;
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
                saveSingleFile(payload, downloadConfig, queue);
            }
        }
        return false;
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

//    private boolean saveMultipleFile(ChannelHandlerContext ctx, PeerWirePayload peerWirePayload) {
//        byte[] block = peerWirePayload.getBlock();
//        // 1.判断该区块的位置是属于哪个文件
//        int skipBytes = peerWirePayload.getIndex() * this.onePieceRequestSum * HandshakeUtils.PIECE_MAX_LENGTH
//                + peerWirePayload.getBegin();
//        int fileIndex = DownloadManagerUtils.getFileIndex(skipBytes, torrent);
//        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
//        // 待处理的字节的大小
//        int length = 7;
//        // 保存文件
//        if (block.length > torrentFileItem.getLength()) {
//            int subBegin = block.length - length;
//            byte[] subBlock = new byte[subBegin];
//            System.arraycopy(block, length, subBlock, 0, subBegin);
//            saveMultipleFile(ctx, new PeerWirePayload(
//                    peerWirePayload.getIndex(), peerWirePayload.getBegin() + subBegin, subBlock));
//        }
//    }


}

package xyz.zzyitj.nbt.bean;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * xyz.zzyitj.nbt.bean
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/13 9:37 上午
 * @since 1.0
 */
public class DownloadConfig {
    /**
     * 文件下载位置
     */
    private String savePath;
    /**
     * 一个区块需要请求的次数
     */
    private int onePieceRequestSize;
    /**
     * 区块下载完成进度
     */
    private boolean[] pieceProcess;
    /**
     * 下载大小
     */
    private AtomicLong downloadSum;
    /**
     * pieceQueue原本大小
     */
    private int pieceQueueSize;
    /**
     * 区块下载队列
     */
    private Queue<RequestPiece> pieceQueue;
    /**
     * 启用下载log
     */
    private boolean showDownloadLog;

    public DownloadConfig() {
    }

    public DownloadConfig(String savePath) {
        this.savePath = savePath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getOnePieceRequestSize() {
        return onePieceRequestSize;
    }

    public void setOnePieceRequestSize(int onePieceRequestSize) {
        this.onePieceRequestSize = onePieceRequestSize;
    }

    public boolean isShowDownloadLog() {
        return showDownloadLog;
    }

    public void setShowDownloadLog(boolean showDownloadLog) {
        this.showDownloadLog = showDownloadLog;
    }

    public Queue<RequestPiece> getPieceQueue() {
        return pieceQueue;
    }

    public void setPieceQueue(Queue<RequestPiece> pieceQueue) {
        this.pieceQueue = pieceQueue;
    }

    public boolean[] getPieceProcess() {
        return pieceProcess;
    }

    public void setPieceProcess(boolean[] pieceProcess) {
        this.pieceProcess = pieceProcess;
    }

    public int getPieceQueueSize() {
        return pieceQueueSize;
    }

    public void setPieceQueueSize(int pieceQueueSize) {
        this.pieceQueueSize = pieceQueueSize;
    }

    public AtomicLong getDownloadSum() {
        return downloadSum;
    }

    public void setDownloadSum(AtomicLong downloadSum) {
        this.downloadSum = downloadSum;
    }
}

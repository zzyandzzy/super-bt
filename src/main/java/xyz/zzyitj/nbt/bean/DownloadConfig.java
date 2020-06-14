package xyz.zzyitj.nbt.bean;

import java.util.Queue;

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
     * 区块下载队列
     */
    private Queue<RequestPiece> pieceQueue;
    /**
     * 区块请求下载个数，也就是区块下载队列的大小
     */
    private int requestPieceSize;

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

    public Queue<RequestPiece> getPieceQueue() {
        return pieceQueue;
    }

    public void setPieceQueue(Queue<RequestPiece> pieceQueue) {
        this.pieceQueue = pieceQueue;
    }

    public int getRequestPieceSize() {
        return requestPieceSize;
    }

    public void setRequestPieceSize(int requestPieceSize) {
        this.requestPieceSize = requestPieceSize;
    }

    public int getOnePieceRequestSize() {
        return onePieceRequestSize;
    }

    public void setOnePieceRequestSize(int onePieceRequestSize) {
        this.onePieceRequestSize = onePieceRequestSize;
    }
}

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
    private int onePieceRequestSum;
    /**
     * 区块下载队列
     */
    private Queue<RequestPiece> queue;

    public DownloadConfig() {
    }

    public DownloadConfig(String savePath, int onePieceRequestSum, Queue<RequestPiece> queue) {
        this.savePath = savePath;
        this.onePieceRequestSum = onePieceRequestSum;
        this.queue = queue;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getOnePieceRequestSum() {
        return onePieceRequestSum;
    }

    public void setOnePieceRequestSum(int onePieceRequestSum) {
        this.onePieceRequestSum = onePieceRequestSum;
    }

    public Queue<RequestPiece> getQueue() {
        return queue;
    }

    public void setQueue(Queue<RequestPiece> queue) {
        this.queue = queue;
    }
}

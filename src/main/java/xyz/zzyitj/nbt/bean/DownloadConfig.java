package xyz.zzyitj.nbt.bean;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 区块下载Map
     */
    private Map<Integer, RequestPiece> pieceMap;
    /**
     * 当前下载下标，即pieceMap的key
     */
    private AtomicInteger downloadIndex;
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

    public Map<Integer, RequestPiece> getPieceMap() {
        return pieceMap;
    }

    public void setPieceMap(Map<Integer, RequestPiece> pieceMap) {
        this.pieceMap = pieceMap;
    }

    public AtomicInteger getDownloadIndex() {
        return downloadIndex;
    }

    public void setDownloadIndex(AtomicInteger downloadIndex) {
        this.downloadIndex = downloadIndex;
    }
}

package xyz.zzyitj.nbt.bean;

import java.util.Map;
import java.util.Queue;
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
    private int onePieceSize;
    /**
     * pieceRequestMap原本大小
     */
    private int pieceRequestMapSize;
    /**
     * 区块下载map
     */
    private Map<Integer, RequestPiece> pieceRequestMap;
    /**
     * 区块下载map的key
     */
    private AtomicInteger pieceRequestMapKey;
    /**
     * 失败的区块请求
     */
    private Queue<RequestPiece> failPieceRequest;
    /**
     * 是否有失败的请求
     */
    private volatile boolean failDownloadPieceRequest;
    /**
     * 在下载完成后是否已经关闭了全部的peer
     */
    private volatile boolean closeAllPeer;
    /**
     * 启用下载log
     */
    private boolean showDownloadLog;
    /**
     * 启用请求log
     */
    private boolean showRequestLog;

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

    public boolean isShowDownloadLog() {
        return showDownloadLog;
    }

    public void setShowDownloadLog(boolean showDownloadLog) {
        this.showDownloadLog = showDownloadLog;
    }


    public int getOnePieceSize() {
        return onePieceSize;
    }

    public void setOnePieceSize(int onePieceSize) {
        this.onePieceSize = onePieceSize;
    }

    public int getPieceRequestMapSize() {
        return pieceRequestMapSize;
    }

    public void setPieceRequestMapSize(int pieceRequestMapSize) {
        this.pieceRequestMapSize = pieceRequestMapSize;
    }

    public Map<Integer, RequestPiece> getPieceRequestMap() {
        return pieceRequestMap;
    }

    public void setPieceRequestMap(Map<Integer, RequestPiece> pieceRequestMap) {
        this.pieceRequestMap = pieceRequestMap;
    }

    public AtomicInteger getPieceRequestMapKey() {
        return pieceRequestMapKey;
    }

    public void setPieceRequestMapKey(AtomicInteger pieceRequestMapKey) {
        this.pieceRequestMapKey = pieceRequestMapKey;
    }

    public boolean isShowRequestLog() {
        return showRequestLog;
    }

    public void setShowRequestLog(boolean showRequestLog) {
        this.showRequestLog = showRequestLog;
    }

    public Queue<RequestPiece> getFailPieceRequest() {
        return failPieceRequest;
    }

    public void setFailPieceRequest(Queue<RequestPiece> failPieceRequest) {
        this.failPieceRequest = failPieceRequest;
    }

    public boolean isFailDownloadPieceRequest() {
        return failDownloadPieceRequest;
    }

    public void setFailDownloadPieceRequest(boolean failDownloadPieceRequest) {
        this.failDownloadPieceRequest = failDownloadPieceRequest;
    }

    public boolean isCloseAllPeer() {
        return closeAllPeer;
    }

    public void setCloseAllPeer(boolean closeAllPeer) {
        this.closeAllPeer = closeAllPeer;
    }
}

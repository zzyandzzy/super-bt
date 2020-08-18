package xyz.zzyitj.nbt.cs;

import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.manager.DownloadManager;
import xyz.zzyitj.nbt.manager.ProgressDownloadManager;

import java.util.List;

/**
 * xyz.zzyitj.nbt.cs
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/8/18 19:59
 * @since 1.0
 */
public abstract class AbstractClientBuilder {
    List<Peer> peerList;
    Torrent torrent;
    String savePath;
    AbstractDownloadManager downloadManager;
    LoggingHandler loggingHandler;
    boolean showDownloadLog;
    boolean showRequestLog;

    public AbstractClientBuilder(List<Peer> peerList, Torrent torrent, String savePath) {
        if (peerList == null || torrent == null || StringUtils.isBlank(savePath)) {
            throw new NullPointerException("ClientBuilder constructor args may null.");
        }
        this.peerList = peerList;
        this.torrent = torrent;
        this.savePath = savePath;
        this.downloadManager = new DownloadManager();
        this.downloadManager.setTorrent(torrent);
    }

    public Client builder() {
        return buildClient();
    }

    protected abstract Client buildClient();

    public AbstractClientBuilder torrent(Torrent torrent) {
        this.torrent = torrent;
        return this;
    }

    public AbstractClientBuilder savePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public AbstractClientBuilder loggingHandler(LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
        return this;
    }

    public AbstractClientBuilder downloadManager(AbstractDownloadManager downloadManager) {
        this.downloadManager = downloadManager;
        return this;
    }

    public AbstractClientBuilder showDownloadLog(boolean showDownloadLog) {
        this.showDownloadLog = showDownloadLog;
        return this;
    }

    public AbstractClientBuilder showRequestLog(boolean showRequestLog) {
        this.showRequestLog = showRequestLog;
        return this;
    }

    public AbstractClientBuilder showDownloadProcess(boolean showDownloadProcess) {
        if (showDownloadProcess) {
            // 装饰者
            this.downloadManager = new ProgressDownloadManager(this.downloadManager);
        }
        return this;
    }
}

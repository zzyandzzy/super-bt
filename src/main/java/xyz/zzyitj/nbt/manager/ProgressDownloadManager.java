package xyz.zzyitj.nbt.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.RequestPiece;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/13 11:06 上午
 * @since 1.0
 */
public class ProgressDownloadManager extends AbstractDownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(ProgressDownloadManager.class);

    private final AbstractDownloadManager delegate;

    public ProgressDownloadManager(AbstractDownloadManager delegate) {
        this.delegate = delegate;
        this.setTorrent(delegate.getTorrent());
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "downloading_progress_monitor");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleAtFixedRate(this::showProcess, 0, 1, TimeUnit.SECONDS);
    }

    private void showProcess() {
        if (getTorrent() == null) {
            return;
        }
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(getTorrent());
        if (downloadConfig == null) {
            return;
        }
        Map<Integer, RequestPiece> pieceMap = downloadConfig.getPieceMap();
        if (pieceMap == null) {
            return;
        }
        // 下载进度
        int downloadIndex = downloadConfig.getDownloadIndex().get();
        float progress = (downloadIndex * 1.0F) / pieceMap.size();
        logger.info("torrent name: {}, downloading progress: {}%",
                getTorrent().getName(), progress * 100);
    }

    @Override
    public boolean save(PeerWirePayload payload) {
        return delegate.save(payload);
    }
}

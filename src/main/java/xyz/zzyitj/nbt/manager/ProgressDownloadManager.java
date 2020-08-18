package xyz.zzyitj.nbt.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Configuration;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.RequestPiece;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    private final ScheduledExecutorService scheduledExecutorService;
    private static final float DOWNLOAD_COMPLETE = 100.0F;

    public ProgressDownloadManager(AbstractDownloadManager delegate) {
        this.delegate = delegate;
        this.setTorrent(delegate.getTorrent());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "downloading_progress_monitor");
                thread.setDaemon(true);
                return thread;
            }
        });
        scheduledExecutorService.scheduleAtFixedRate(this::showProcess, 0, 1, TimeUnit.SECONDS);
    }

    private void showProcess() {
        if (getTorrent() == null) {
            return;
        }
        DownloadConfig downloadConfig = Configuration.downloadConfigMap.get(getTorrent());
        if (downloadConfig == null) {
            return;
        }
        Map<Integer, RequestPiece> pieceRequestMap = downloadConfig.getPieceRequestMap();
        if (pieceRequestMap == null) {
            return;
        }
        // 下载进度
        float progress = ((downloadConfig.getPieceRequestMapSize() - pieceRequestMap.size()) * 1.0F)
                / downloadConfig.getPieceRequestMapSize() * 100;
        logger.info("torrent name: {}, downloading progress: {}%",
                getTorrent().getName(), progress);
        if (progress >= DOWNLOAD_COMPLETE) {
            scheduledExecutorService.shutdownNow();
        }
    }

    @Override
    public boolean save(PeerWirePayload payload) {
        return delegate.save(payload);
    }
}

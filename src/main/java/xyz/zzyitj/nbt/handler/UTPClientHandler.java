package xyz.zzyitj.nbt.handler;

import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/14 3:26 下午
 * @since 1.0
 */
public class UTPClientHandler extends AbstractUTPHandler {
    public UTPClientHandler(Torrent torrent, AbstractDownloadManager downloadManager) {
        this.torrent = torrent;
        this.downloadManager = downloadManager;
    }
}

package xyz.zzyitj.nbt.manager;

import xyz.zzyitj.nbt.bean.PeerWirePayload;
import xyz.zzyitj.nbt.bean.Torrent;

/**
 * xyz.zzyitj.nbt.handler
 * 下载管理器
 * <p>
 * 存储文件什么的
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:52 下午
 * @since 1.0
 */
public abstract class AbstractDownloadManager {
    private Torrent torrent;

    /**
     * 设置种子
     *
     * @param torrent 种子
     */
    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    /**
     * 获取种子
     *
     * @return 种子
     */
    public Torrent getTorrent() {
        return torrent;
    }

    /**
     * 存储字节流到文件
     *
     * @param payload 字节流信息等
     * @return false文件还没下载完，true文件以及下载完了
     */
    public abstract boolean save(PeerWirePayload payload);
}

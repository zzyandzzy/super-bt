package xyz.zzyitj.nbt.handler;

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
public interface DownloadManager {
    /**
     * 设置种子
     *
     * @param torrent 种子
     */
    void setTorrent(Torrent torrent);

    /**
     * 存储字节流到文件
     *
     * @param payload 字节流信息等
     * @return false文件还没下载完，true文件以及下载完了
     */
    boolean saveBytesToFile(PeerWirePayload payload);
}

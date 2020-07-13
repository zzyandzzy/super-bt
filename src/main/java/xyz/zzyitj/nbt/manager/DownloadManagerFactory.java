package xyz.zzyitj.nbt.manager;

/**
 * xyz.zzyitj.nbt.manager
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/13 11:45 上午
 * @since 1.0
 */
public interface DownloadManagerFactory {
    /**
     * 获取DownloadManager
     *
     * @return
     */
    AbstractDownloadManager getDownloadManager();
}

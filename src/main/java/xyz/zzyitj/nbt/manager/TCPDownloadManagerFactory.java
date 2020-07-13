package xyz.zzyitj.nbt.manager;

/**
 * xyz.zzyitj.nbt.manager
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/13 11:44 上午
 * @since 1.0
 */
public class TCPDownloadManagerFactory implements DownloadManagerFactory {
    @Override
    public AbstractDownloadManager getDownloadManager() {
        return new TCPDownloadManager();
    }
}

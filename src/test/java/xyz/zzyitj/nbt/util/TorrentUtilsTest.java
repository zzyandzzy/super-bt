package xyz.zzyitj.nbt.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/5/30 5:45 下午
 * @since 1.0
 */
public class TorrentUtilsTest {

    /**
     * 测试根据种子得到torrent信息
     * {@link TorrentUtils#getTorrent(File)}
     *
     * @throws IOException 文件打开错误
     */
    @Test
    public void getTorrent() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("torrents/onefile_onepiece.torrent");
        Assert.assertNotNull(url);
        File file = new File(url.getPath());
        Assert.assertNotNull(TorrentUtils.getTorrent(file));
    }
}
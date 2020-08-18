package xyz.zzyitj.nbt.util;

import org.junit.Before;
import org.junit.Test;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.bean.TorrentFileItem;

import java.io.File;
import java.io.IOException;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/14 9:46 下午
 * @since 1.0
 */
public class DownloadManagerUtilsTest {
    private static Torrent torrent;

    @Before
    public void init() throws IOException {
        String torrentPath = "/Users/intent/Desktop/sbt/多个文件多个区块.torrent";
        File torrentFile = new File(torrentPath);
        torrent = TorrentUtils.getTorrent(torrentFile);
        for (TorrentFileItem torrentFileItem : torrent.getTorrentFileItemList()) {
            System.out.println(torrentFileItem);
        }
    }

    @Test
    public void getStartPosition() {
        System.out.println(DownloadManagerUtils.getStartPosition(0, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(15120, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(15246, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(16384, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(32768, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(47888, torrent));
        System.out.println(DownloadManagerUtils.getStartPosition(65536, torrent));
    }
}
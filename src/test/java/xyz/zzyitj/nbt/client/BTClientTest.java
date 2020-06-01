package xyz.zzyitj.nbt.client;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.TorrentUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:15 下午
 * @email zzy.main@gmail.com
 */
class BTClientTest {

    /**
     * 测试握手
     *
     * @throws InterruptedException 连接失败
     */
    @Test
    void testHandshake() throws InterruptedException, IOException {
        String torrentPath = "/Users/intent/Desktop/sbt/test.torrent";
        String savePath = "./test/";
        File torrentFile = new File(torrentPath);
        Torrent torrent = TorrentUtils.getTorrent(torrentFile);
        // 创建文件夹
        if (torrent.getTorrentFileItemList() != null) {
            torrent.getTorrentFileItemList().forEach(torrentFileItem -> {
                try {
                    File file = new File(savePath +
                            torrent.getName() + File.separator + torrentFileItem.getPath());
                    // 创建文件夹
                    FileUtils.forceMkdirParent(file);
                    // 创建空文件
                    if (torrentFileItem.getLength() == 0) {
                        FileUtils.writeByteArrayToFile(file, new byte[0]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        BTClient client = new BTClient(Const.TEST_HOST, Const.TEST_PORT, torrent, savePath);
        client.start();
    }

}
package xyz.zzyitj.nbt.client;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
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
class ClientTest {
    private static final String savePath = "./test/";
    private static Torrent torrent;

    @BeforeAll
    static void init() throws IOException {
        String torrentPath = "/Users/intent/Desktop/sbt/test.torrent";
        File torrentFile = new File(torrentPath);
        torrent = TorrentUtils.getTorrent(torrentFile);
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
    }

    /**
     * 测试TCPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    void testTCPClient() throws InterruptedException {
        Client client = new TCPClient.TCPClientBuilder(Const.TEST_HOST, Const.TEST_PORT)
                .torrent(torrent)
                .savePath(savePath)
                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        client.start();
    }

    /**
     * 测试UTPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    void testUTPClient() throws InterruptedException {
        Client client = new UTPClient.UTPClientBuilder(Const.TEST_HOST, Const.TEST_PORT)
                .torrent(torrent)
                .savePath(savePath)
                .builder();
        client.start();
    }

}
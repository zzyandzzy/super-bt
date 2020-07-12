package xyz.zzyitj.nbt.client;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.handler.DownloadManager;
import xyz.zzyitj.nbt.handler.TCPDownloadManager;
import xyz.zzyitj.nbt.util.TorrentUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:15 下午
 */
class ClientTest {
    // Local Transmission/qBitTorrent host
    public static final String TEST_IP = "127.0.0.1";
    // Local qBitTorrent port
    public static final int TEST_PORT = 18357;
    // Local Transmission port
//    public static final int TEST_PORT = 51413;

    private static final String savePath = "./download/";
    private static Torrent torrent;
    private static DownloadManager downloadManager;

    @BeforeAll
    static void init() throws IOException {
        String torrentPath = "/Users/intent/Desktop/sbt/一个文件一个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/一个文件多个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/多个文件一个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/多个文件多个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/test.torrent";
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
        downloadManager = new TCPDownloadManager();
    }

    /**
     * 测试TCPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    void testTCPClient() throws InterruptedException {
        List<Peer> peerList = new ArrayList<>();
//        peerList.add(new Peer(TEST_IP, 51413));
        peerList.add(new Peer(TEST_IP, 18357));
        Client client = new TCPClient.TCPClientBuilder(peerList, torrent, savePath, downloadManager)
                .showDownloadLog(false)
                .showDownloadProcess(true)
//                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        client.start();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试UTPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    void testUTPClient() throws InterruptedException {
        Client client = new UTPClient.UTPClientBuilder(TEST_IP, TEST_PORT, torrent, savePath)
                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        client.start();
    }

}
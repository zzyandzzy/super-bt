package xyz.zzyitj.nbt.client;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.manager.TCPDownloadManagerFactory;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.TorrentUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:15 下午
 */
class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);
    // Local Transmission/qBitTorrent host
    public static final String TEST_IP = "127.0.0.1";
    // Local qBitTorrent port
    public static final int TEST_PORT = 18357;
    // Local Transmission port
//    public static final int TEST_PORT = 51413;

    private static final String savePath = "./download/";
    private static Torrent torrent;
    private static AbstractDownloadManager downloadManager;

    @BeforeAll
    static void init() throws IOException {
//        String torrentPath = "/Users/intent/Desktop/sbt/一个文件一个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/一个文件多个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/多个文件一个区块.torrent";
//        String torrentPath = "/Users/intent/Desktop/sbt/多个文件多个区块.torrent";
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
        downloadManager = new TCPDownloadManagerFactory().getDownloadManager();
    }

    /**
     * 测试TCPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    void testTCPClient() throws InterruptedException {
        startReport();
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
     * 监控堆外内存
     */
    private void startReport() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "netty_direct_memory_monitor");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleAtFixedRate(this::doReport, 0, 5, TimeUnit.SECONDS);
    }

    private void doReport() {
        logger.info("current netty direct memory: {}B, {}KB, {}MB, {}GB",
                PlatformDependent.usedDirectMemory(),
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_KB,
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_MB,
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_GB);
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
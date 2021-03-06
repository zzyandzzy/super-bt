package xyz.zzyitj.nbt;

import io.netty.util.internal.PlatformDependent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.ByteUtils;
import xyz.zzyitj.nbt.util.TorrentUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * xyz.zzyitj.nbt
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/8/18 10:14
 * @since 1.0
 */
public class ConfigTest {
    private static final Logger logger = LoggerFactory.getLogger(ConfigTest.class);
    // Local Transmission/qBitTorrent host
    public static final String TEST_IP = "127.0.0.1";
    // Local qBitTorrent port
    public static final int TEST_PORT = 18357;
    // Local Transmission port
//    public static final int TEST_PORT = 51413;

    public static final String SAVE_PATH = "./download/";

    public static Torrent torrent;
    public static List<Peer> peerList;

    public static void init() throws IOException {
        URL url = ConfigTest.class.getClassLoader().getResource("torrents/onefile_onepiece.torrent");
//        URL url = ConfigTest.class.getClassLoader().getResource("torrents/onefile_multiplepiece.torrent");
//        URL url = ConfigTest.class.getClassLoader().getResource("torrents/multiplefile_onepiece.torrent");
//        URL url = ConfigTest.class.getClassLoader().getResource("torrents/multiplefile_multiplepiece.torrent");
//        URL url = ConfigTest.class.getClassLoader().getResource("torrents/test.torrent");
//        Assert.assertNotNull(url);
        String torrentPath = url.getPath();
//        String torrentPath = "/Users/intent/Documents/sbt/test/712m-4file.torrent";
        File torrentFile = new File(torrentPath);
        torrent = TorrentUtils.getTorrent(torrentFile);
        // 创建文件夹
        if (torrent.getTorrentFileItemList() != null) {
            torrent.getTorrentFileItemList().forEach(torrentFileItem -> {
                try {
                    File file = new File(ConfigTest.SAVE_PATH +
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

        ConfigTest.startReport();
        peerList = new ArrayList<>();
        ConfigTest.addPeers(peerList);
//        ConfigTest.addPeers(peerList, torrentPath);
    }

    /**
     * 监控堆外内存
     */
    public static void startReport() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "netty_direct_memory_monitor");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleAtFixedRate(ConfigTest::doReport, 0, 5, TimeUnit.SECONDS);
    }

    private static void doReport() {
        logger.info("current netty direct memory: {}B, {}KB, {}MB, {}GB",
                PlatformDependent.usedDirectMemory(),
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_KB,
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_MB,
                PlatformDependent.usedDirectMemory() / ByteUtils.BYTE_GB);
    }

    public static void addPeers(List<Peer> peerList) {
//                peerList.add(new Peer(TEST_IP, 51413));
        peerList.add(new Peer(TEST_IP, 18357));
    }

    public static void addPeers(List<Peer> peerList, String torrentPath) throws IOException {
        String peerTxtPath = torrentPath.replace(".torrent", ".txt");
        File file = new File(peerTxtPath);
        if (file.exists()) {
            String s = readFile(file);
            if (StringUtils.isNotBlank(s)) {
                String[] lines = s.split("\n");
                for (String line : lines) {
                    if (StringUtils.isNotBlank(StringUtils.deleteWhitespace(line))) {
                        String[] ip = line.split(":");
                        if (ip.length == 2) {
                            peerList.add(new Peer(ip[0], Integer.parseInt(ip[1])));
                        }
                    }
                }
            }
        }
    }

    public static String readFile(File file) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            StringBuilder sb = new StringBuilder();
            fileInputStream = new FileInputStream(file);
            byte[] b = new byte[1024];
            while (fileInputStream.read(b) != -1) {
                sb.append(new String(b));
            }
            return sb.toString();
        } catch (Exception ignored) {
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return null;
    }
}

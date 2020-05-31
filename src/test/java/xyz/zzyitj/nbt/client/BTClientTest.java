package xyz.zzyitj.nbt.client;

import org.junit.jupiter.api.Test;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.util.Const;
import xyz.zzyitj.nbt.util.TorrentUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

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
        File file = new File("/Users/intent/Desktop/sbt/2.torrent");
        Torrent torrent = TorrentUtils.getTorrent(file);
        System.out.println(torrent);
        BTClient client = new BTClient(Const.TEST_HOST, Const.TEST_PORT, torrent);
        client.start();
    }

}
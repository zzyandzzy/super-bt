package xyz.zzyitj.nbt.client;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Before;
import org.junit.Test;
import xyz.zzyitj.nbt.ConfigTest;
import xyz.zzyitj.nbt.cs.Client;
import xyz.zzyitj.nbt.protocol.utp.cs.UtpClient;

import java.io.IOException;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/8/18 10:25
 * @since 1.0
 */
public class UDPClientTest {

    @Before
    public void before() throws IOException {
        ConfigTest.init();
    }

    /**
     * 测试UTPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    public void testUTPClient() throws InterruptedException {
        Client client = new UtpClient.Builder(ConfigTest.peerList, ConfigTest.torrent, ConfigTest.SAVE_PATH)
                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        client.start();
    }
}

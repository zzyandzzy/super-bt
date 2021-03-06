package xyz.zzyitj.nbt.client;

import org.junit.Before;
import org.junit.Test;
import xyz.zzyitj.nbt.ConfigTest;
import xyz.zzyitj.nbt.cs.Client;
import xyz.zzyitj.nbt.cs.TcpClient;

import java.io.IOException;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/8/18 10:13
 * @since 1.0
 */
public class TCPClientTest {

    @Before
    public void before() throws IOException {
        ConfigTest.init();
    }

    /**
     * 测试TCPClient
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    public void testTCPClient() throws InterruptedException {
        Client client = new TcpClient.Builder(ConfigTest.peerList, ConfigTest.torrent, ConfigTest.SAVE_PATH)
                .showDownloadLog(false)
                .showRequestLog(false)
                .showDownloadProcess(true)
//                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        client.start();
    }
}

package xyz.zzyitj.nbt.server;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;
import xyz.zzyitj.nbt.util.Const;

/**
 * xyz.zzyitj.nbt.server
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 7:25 下午
 * @since 1.0
 */
public class ServerTest {
    /**
     * 测试TCPServer
     *
     * @throws InterruptedException 连接错误
     */
    @Test
    public void testTCPServer() throws InterruptedException {
        Server server = new TCPServer.TCPServerBuilder(Const.SERVER_PORT, null)
                .loggingHandler(new LoggingHandler(LogLevel.INFO))
                .builder();
        server.start();
    }
}
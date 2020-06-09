package xyz.zzyitj.nbt.server;

import io.netty.handler.logging.LoggingHandler;

import java.util.List;

/**
 * xyz.zzyitj.nbt.server
 * BT协议的UTP实现服务器类
 * 使用Builder设计模式
 * <p>
 * http://www.bittorrent.org/beps/bep_0029.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 9:10 上午
 * @since 1.0
 */
public class UTPServer implements Server {
    private int port;
    /**
     * 做种可能多个路径
     */
    private List<String> savePath;
    private LoggingHandler loggingHandler;

    public UTPServer(UTPServerBuilder builder) {
        this.port = builder.port;
        this.savePath = builder.savePath;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public void start() throws InterruptedException {
    }

    static class UTPServerBuilder {
        private int port;
        /**
         * 做种可能多个路径
         */
        private List<String> savePath;
        private LoggingHandler loggingHandler;

        public UTPServer builder() {
            return new UTPServer(this);
        }

        public UTPServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public UTPServerBuilder savePath(List<String> savePath) {
            this.savePath = savePath;
            return this;
        }

        public UTPServerBuilder loggingHandler(LoggingHandler loggingHandler) {
            this.loggingHandler = loggingHandler;
            return this;
        }
    }
}

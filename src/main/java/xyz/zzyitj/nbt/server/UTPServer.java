package xyz.zzyitj.nbt.server;

import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;

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
    private List<Torrent> torrentList;
    private LoggingHandler loggingHandler;

    public UTPServer(UTPServerBuilder builder) {
        this.port = builder.port;
        this.torrentList = builder.torrentList;
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
        private List<Torrent> torrentList;
        private LoggingHandler loggingHandler;

        public UTPServerBuilder(int port, List<Torrent> torrentList) {
            this.port = port;
            this.torrentList = torrentList;
        }

        public UTPServer builder() {
            return new UTPServer(this);
        }

        public UTPServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public UTPServerBuilder torrentList(List<Torrent> torrentList) {
            this.torrentList = torrentList;
            return this;
        }

        public UTPServerBuilder loggingHandler(LoggingHandler loggingHandler) {
            this.loggingHandler = loggingHandler;
            return this;
        }
    }
}

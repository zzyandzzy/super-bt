package xyz.zzyitj.nbt.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;

import java.net.InetSocketAddress;
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
    private final int port;
    /**
     * 做种的种子list
     */
    private final List<Torrent> torrentList;
    private final LoggingHandler loggingHandler;

    public UTPServer(UTPServerBuilder builder) {
        this.port = builder.port;
        this.torrentList = builder.torrentList;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    if (loggingHandler != null) {
                        p.addLast("logger", loggingHandler);
                    }
                }
            });
            ChannelFuture f = b.bind(new InetSocketAddress(this.port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class UTPServerBuilder {
        private int port;
        /**
         * 做种的种子list
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

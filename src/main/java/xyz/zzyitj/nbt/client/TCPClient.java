package xyz.zzyitj.nbt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.codec.PeerWireProtocolDecoder;
import xyz.zzyitj.nbt.handler.DownloadManager;
import xyz.zzyitj.nbt.handler.TCPClientHandler;
import xyz.zzyitj.nbt.util.PeerWireConst;

import java.net.InetSocketAddress;

/**
 * xyz.zzyitj.nbt.client
 * BT协议的TCP实现客户端类
 * 使用Builder设计模式
 * <p>
 * http://www.bittorrent.org/beps/bep_0003.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 8:23 上午
 * @since 1.0
 */
public class TCPClient implements Client {
    private final String ip;
    private final int port;
    private final Torrent torrent;
    private final String savePath;
    private final DownloadManager downloadManager;
    private final LoggingHandler loggingHandler;

    public TCPClient(TCPClientBuilder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.torrent = builder.torrent;
        this.savePath = builder.savePath;
        this.downloadManager = builder.downloadManager;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public String toString() {
        return "TCPClient{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", torrent=" + torrent +
                ", savePath='" + savePath + '\'' +
                ", downloadManager='" + downloadManager + '\'' +
                ", loggingHandler=" + loggingHandler +
                '}';
    }

    @Override
    public void start() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (loggingHandler != null) {
                                p.addLast("logging", loggingHandler);
                            }
                            p.addLast(new PeerWireProtocolDecoder(PeerWireConst.PEER_WIRE_MAX_FRAME_LENGTH,
                                    0, 4, 0, 0, false));
                            if (Application.downloadConfigMap.get(torrent) == null) {
                                Application.downloadConfigMap.put(torrent, new DownloadConfig(savePath, 0, null));
                            }
                            p.addLast(new TCPClientHandler.TCPClientHandlerBuilder(torrent, downloadManager)
                                    .build());
                        }
                    });
            ChannelFuture f = b.connect(new InetSocketAddress(this.ip, this.port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    static class TCPClientBuilder {
        private String ip;
        private int port;
        private Torrent torrent;
        private String savePath;
        private DownloadManager downloadManager;
        private LoggingHandler loggingHandler;

        public TCPClientBuilder(String ip, int port, Torrent torrent, String savePath, DownloadManager downloadManager) {
            this.ip = ip;
            this.port = port;
            this.torrent = torrent;
            this.savePath = savePath;
            this.downloadManager = downloadManager;
        }

        TCPClient builder() {
            return new TCPClient(this);
        }

        public TCPClientBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public TCPClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public TCPClientBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public TCPClientBuilder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public TCPClientBuilder loggingHandler(LoggingHandler loggingHandler) {
            this.loggingHandler = loggingHandler;
            return this;
        }

        public TCPClientBuilder downloadManager(DownloadManager downloadManager) {
            this.downloadManager = downloadManager;
            return this;
        }
    }
}

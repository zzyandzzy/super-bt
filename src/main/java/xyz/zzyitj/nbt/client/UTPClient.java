package xyz.zzyitj.nbt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;

import java.net.InetSocketAddress;

/**
 * xyz.zzyitj.nbt.client
 * BT协议的UTP实现客服端
 * 使用Builder设计模式
 * <p>
 * http://www.bittorrent.org/beps/bep_0029.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 8:45 上午
 * @since 1.0
 */
public class UTPClient implements Client {
    private String ip;
    private int port;
    private Torrent torrent;
    private String savePath;
    private LoggingHandler loggingHandler;

    private EventLoopGroup workGroup;

    public UTPClient(UTPClientBuilder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.torrent = builder.torrent;
        this.savePath = builder.savePath;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public String toString() {
        return "UTPClient{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", torrent=" + torrent +
                ", savePath='" + savePath + '\'' +
                ", loggingHandler=" + loggingHandler +
                '}';
    }

    @Override
    public void start() throws InterruptedException {
        workGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workGroup)
                    // 通过NioDatagramChannel创建Channel，并设置Socket参数支持广播
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true);
            // UDP相对于TCP不需要在客户端和服务端建立实际的连接
            // 因此不需要为连接（ChannelPipeline）设置handler
//                    .handler();
            ChannelFuture f = b.connect(new InetSocketAddress(this.ip, this.port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    static class UTPClientBuilder {
        private String ip;
        private int port;
        private Torrent torrent;
        private String savePath;
        private LoggingHandler loggingHandler;

        public UTPClientBuilder(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        UTPClient builder() {
            return new UTPClient(this);
        }

        public UTPClientBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public UTPClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public UTPClientBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public UTPClientBuilder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public UTPClientBuilder loggingHandler(LoggingHandler loggingHandler) {
            this.loggingHandler = loggingHandler;
            return this;
        }
    }
}

package xyz.zzyitj.nbt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.codec.PeerWireProtocolDecoder;
import xyz.zzyitj.nbt.util.HandshakeUtils;

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
    private String ip;
    private int port;
    private Torrent torrent;
    private String savePath;
    private LoggingHandler loggingHandler;

    private EventLoopGroup workGroup;

    /**
     * 这里帧最大长度加13是因为当帧id为7时
     * 帧长度为HandshakeUtils.PIECE_MAX_LENGTH + 4个byte头部length + 1个byte的id + 4个byte的index + 4个byte的begin
     * {@link HandshakeUtils#PIECE}
     */
    private final int MAX_FRAME_LENGTH = HandshakeUtils.PIECE_MAX_LENGTH + 13;

    public TCPClient(TCPClientBuilder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.torrent = builder.torrent;
        this.savePath = builder.savePath;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public String toString() {
        return "TCPClient{" +
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
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            if (loggingHandler != null) {
                                ch.pipeline().addLast("logging", loggingHandler);
                            }
                            ch.pipeline().addLast(
                                    new PeerWireProtocolDecoder(MAX_FRAME_LENGTH,
                                            0, 4, 0, 0, false));
                            ch.pipeline().addLast(new TCPClientHandler(torrent, savePath));
                        }
                    });
            ChannelFuture f = b.connect(new InetSocketAddress(this.ip, this.port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    static class TCPClientBuilder {
        private String ip;
        private int port;
        private Torrent torrent;
        private String savePath;
        private LoggingHandler loggingHandler;

        public TCPClientBuilder(String ip, int port) {
            this.ip = ip;
            this.port = port;
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
    }
}

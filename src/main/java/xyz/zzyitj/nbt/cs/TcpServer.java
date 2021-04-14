package xyz.zzyitj.nbt.cs;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.codec.PeerWireProtocolDecoder;
import xyz.zzyitj.nbt.handler.TcpServerHandler;
import xyz.zzyitj.nbt.util.PeerWireConst;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * BT协议的TCP实现服务器类
 * 使用Builder设计模式
 * <p>
 * http://www.bittorrent.org/beps/bep_0003.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 9:04 上午
 * @since 1.0
 */
public class TcpServer implements Server {
    private final int port;
    /**
     * 做种的种子list
     */
    private final List<Torrent> torrentList;
    private final LoggingHandler loggingHandler;

    public TcpServer(Builder builder) {
        this.port = builder.port;
        this.torrentList = builder.torrentList;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public String toString() {
        return "TCPServer{" +
                "port=" + port +
                ", torrentList=" + torrentList +
                ", loggingHandler=" + loggingHandler +
                '}';
    }

    @Override
    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    if (loggingHandler != null) {
                        p.addLast("logger", loggingHandler);
                    }
                    p.addLast(new PeerWireProtocolDecoder(PeerWireConst.FRAME_MAX_LENGTH,
                            0, PeerWireConst.PACKAGE_HEADER_LENGTH, 0, 0, false));
                    p.addLast(new TcpServerHandler(torrentList));
                }
            });
            ChannelFuture f = b.bind(new InetSocketAddress(this.port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class Builder extends AbstractServerBuilder {

        public Builder(int port, List<Torrent> torrentList) {
            super(port, torrentList);
        }

        @Override
        protected Server buildServer() {
            return new TcpServer(this);
        }
    }
}

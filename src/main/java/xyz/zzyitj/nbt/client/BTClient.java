package xyz.zzyitj.nbt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.bean.Torrent;

import java.net.InetSocketAddress;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:14 下午
 * @email zzy.main@gmail.com
 */
public class BTClient {
    private final String host;
    private final int port;
    private final Torrent torrent;
    private final String savePath;

    public BTClient(String host, int port, Torrent torrent, String savePath) {
        this.host = host;
        this.port = port;
        this.torrent = torrent;
        this.savePath = savePath;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("logging", new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new BTClientHandler(torrent, savePath));
                        }
                    });
            ChannelFuture f = b.connect(new InetSocketAddress(host, port)).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

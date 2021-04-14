package xyz.zzyitj.nbt.protocol.utp.cs;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.Configuration;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.cs.AbstractClientBuilder;
import xyz.zzyitj.nbt.cs.Client;
import xyz.zzyitj.nbt.protocol.utp.handler.UtpClientHandler;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * BT协议的UTP实现客服端
 * 使用Builder设计模式
 * <p>
 * http://www.bittorrent.org/beps/bep_0029.html
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 8:45 上午
 * @since 1.0
 */
public class UtpClient implements Client {
    private final List<Peer> peerList;
    private final Torrent torrent;
    private final String savePath;
    private final AbstractDownloadManager downloadManager;
    private final LoggingHandler loggingHandler;

    public UtpClient(Builder builder) {
        this.peerList = builder.getPeerList();
        this.torrent = builder.getTorrent();
        this.savePath = builder.getSavePath();
        this.downloadManager = builder.getDownloadManager();
        this.loggingHandler = builder.getLoggingHandler();
    }

    @Override
    public void start() throws InterruptedException {
        if (Configuration.downloadConfigMap.get(torrent) == null) {
            DownloadConfig downloadConfig = new DownloadConfig(savePath);
            Configuration.downloadConfigMap.put(torrent, downloadConfig);
        }
        Peer peer = peerList.get(0);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    // 通过NioDatagramChannel创建Channel，并设置Socket参数支持广播
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (loggingHandler != null) {
                                p.addLast("logging", loggingHandler);
                            }
                            p.addLast("handler", new UtpClientHandler(torrent, downloadManager));
                        }
                    });
            ChannelFuture f = b.connect(new InetSocketAddress(peer.getIp(), peer.getPort())).sync();
            f.channel().closeFuture().sync();
            f.addListener(future -> {
                if (!future.isSuccess()) {
                    System.err.printf("%s:%d connect fail!", peer.getIp(), peer.getPort());
                }
            });
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static class Builder extends AbstractClientBuilder {

        public Builder(List<Peer> peerList, Torrent torrent, String savePath) {
            super(peerList, torrent, savePath);
        }

        @Override
        protected Client buildClient() {
            return new UtpClient(this);
        }
    }
}

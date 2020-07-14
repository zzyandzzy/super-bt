package xyz.zzyitj.nbt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.handler.UTPClientHandler;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;

import java.net.InetSocketAddress;
import java.util.List;

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
    private final List<Peer> peerList;
    private final Torrent torrent;
    private final String savePath;
    private final AbstractDownloadManager downloadManager;
    private final LoggingHandler loggingHandler;

    public UTPClient(UTPClientBuilder builder) {
        this.peerList = builder.peerList;
        this.torrent = builder.torrent;
        this.savePath = builder.savePath;
        this.downloadManager = builder.downloadManager;
        this.loggingHandler = builder.loggingHandler;
    }

    @Override
    public void start() throws InterruptedException {
        if (Application.downloadConfigMap.get(torrent) == null) {
            DownloadConfig downloadConfig = new DownloadConfig(savePath);
            Application.downloadConfigMap.put(torrent, downloadConfig);
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
                            p.addLast("handler", new UTPClientHandler(torrent, downloadManager));
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

    static class UTPClientBuilder {
        private final List<Peer> peerList;
        private Torrent torrent;
        private String savePath;
        private AbstractDownloadManager downloadManager;
        private LoggingHandler loggingHandler;

        public UTPClientBuilder(List<Peer> peerList, Torrent torrent, String savePath, AbstractDownloadManager downloadManager) {
            if (peerList == null || torrent == null || StringUtils.isBlank(savePath) || downloadManager == null) {
                throw new NullPointerException("UTPClientBuilder constructor args may null.");
            }
            this.peerList = peerList;
            this.torrent = torrent;
            this.savePath = savePath;
            this.downloadManager = downloadManager;
            this.downloadManager.setTorrent(torrent);
        }

        UTPClient builder() {
            return new UTPClient(this);
        }

        public UTPClientBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public UTPClientBuilder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public UTPClientBuilder downloadManager(AbstractDownloadManager downloadManager) {
            this.downloadManager = downloadManager;
            return this;
        }

        public UTPClientBuilder loggingHandler(LoggingHandler loggingHandler) {
            this.loggingHandler = loggingHandler;
            return this;
        }
    }
}

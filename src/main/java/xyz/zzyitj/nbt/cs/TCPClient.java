package xyz.zzyitj.nbt.cs;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import xyz.zzyitj.nbt.Configuration;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Peer;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.codec.PeerWireProtocolDecoder;
import xyz.zzyitj.nbt.manager.AbstractDownloadManager;
import xyz.zzyitj.nbt.handler.TCPClientHandler;
import xyz.zzyitj.nbt.util.PeerWireConst;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final List<Peer> peerList;
    private final Torrent torrent;
    private final String savePath;
    private final AbstractDownloadManager downloadManager;
    private final LoggingHandler loggingHandler;
    private final boolean showDownloadLog;
    private final boolean showRequestLog;

    private final ExecutorService es;

    public TCPClient(Builder builder) {
        this.peerList = builder.peerList;
        es = Executors.newFixedThreadPool(peerList.size());
        this.torrent = builder.torrent;
        this.savePath = builder.savePath;
        this.downloadManager = builder.downloadManager;
        this.loggingHandler = builder.loggingHandler;
        this.showDownloadLog = builder.showDownloadLog;
        this.showRequestLog = builder.showRequestLog;
    }

    @Override
    public void start() {
        if (Configuration.downloadConfigMap.get(torrent) == null) {
            DownloadConfig downloadConfig = new DownloadConfig(savePath);
            downloadConfig.setShowDownloadLog(this.showDownloadLog);
            downloadConfig.setShowRequestLog(this.showRequestLog);
            Configuration.downloadConfigMap.put(torrent, downloadConfig);
        }
        for (Peer peer : peerList) {
            es.execute(new TCPClientTask(peer));
        }
        es.shutdown();
    }

    class TCPClientTask implements Runnable {
        private Peer peer;

        public TCPClientTask(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (loggingHandler != null) {
                            p.addLast("logging", loggingHandler);
                        }
                        PeerWireProtocolDecoder decoder = new PeerWireProtocolDecoder(PeerWireConst.PEER_WIRE_MAX_FRAME_LENGTH,
                                0, 4, 0, 0, false);
                        p.addLast("decoder", decoder);
                        p.addLast("handler", new TCPClientHandler(torrent, downloadManager));
                    }
                });
                ChannelFuture f = bootstrap.connect(new InetSocketAddress(peer.getIp(), peer.getPort())).sync();
                f.channel().closeFuture().sync();
                f.addListener(future -> {
                    if (!future.isSuccess()) {
                        System.err.printf("%s:%d connect fail!", peer.getIp(), peer.getPort());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public static class Builder extends AbstractClientBuilder {
        public Builder(List<Peer> peerList, Torrent torrent, String savePath) {
            super(peerList, torrent, savePath);
        }

        @Override
        protected Client buildClient() {
            return new TCPClient(this);
        }
    }
}

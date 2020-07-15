package xyz.zzyitj.nbt.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Torrent;

import java.util.Iterator;
import java.util.List;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/7/15 7:19 下午
 * @since 1.0
 */
public class HandlerUtils {
    private static final Logger logger = LoggerFactory.getLogger(HandlerUtils.class);

    /**
     * 关闭连接
     *
     * @param ctx ctx
     */
    public static boolean closePeer(ChannelHandlerContext ctx) {
        if (ctx == null) {
            return false;
        }
        // 关闭这个peer
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
        return true;
    }

    /**
     * 关闭所有连接
     */
    public static void closeAllPeer(Torrent torrent) {
        List<ChannelHandlerContext> peerList = Application.peerMap.get(torrent);
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(torrent);
        if (peerList != null && peerList.size() > 0) {
            Iterator<ChannelHandlerContext> iterator = peerList.iterator();
            while (iterator.hasNext()) {
                ChannelHandlerContext ctx = iterator.next();
                if (closePeer(ctx)) {
                    if (downloadConfig != null && downloadConfig.isShowRequestLog()) {
                        logger.info("{} close, peer list size: {}", ctx.channel().remoteAddress(), peerList.size());
                    }
                    iterator.remove();
                }
            }
        }
    }
}

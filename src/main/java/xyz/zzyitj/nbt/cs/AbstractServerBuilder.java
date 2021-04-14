package xyz.zzyitj.nbt.cs;

import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import xyz.zzyitj.nbt.bean.Torrent;

import java.util.List;

/**
 * xyz.zzyitj.nbt.cs
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/8/18 20:05
 * @since 1.0
 */
@Getter
public abstract class AbstractServerBuilder {
    int port;
    /**
     * 做种的种子list
     */
    List<Torrent> torrentList;
    LoggingHandler loggingHandler;

    public AbstractServerBuilder(int port, List<Torrent> torrentList) {
        if (port < 0 || torrentList.isEmpty()) {
            throw new IllegalArgumentException("port should greater than 0, current port: " + port
                    + ", or torrent list not empty.");
        }
        this.port = port;
        this.torrentList = torrentList;
    }

    public Server builder() {
        return buildServer();
    }

    protected abstract Server buildServer();

    public AbstractServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public AbstractServerBuilder torrentList(List<Torrent> torrentList) {
        this.torrentList = torrentList;
        return this;
    }

    public AbstractServerBuilder loggingHandler(LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
        return this;
    }
}

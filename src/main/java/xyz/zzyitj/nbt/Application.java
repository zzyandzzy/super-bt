package xyz.zzyitj.nbt;

import xyz.zzyitj.nbt.bean.DownloadConfig;
import xyz.zzyitj.nbt.bean.Torrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 7:14 下午
 * @email zzy.main@gmail.com
 */
public class Application {
    /**
     * 下载配置Map
     */
    public static Map<Torrent, DownloadConfig> downloadConfigMap = new ConcurrentHashMap<>();
}

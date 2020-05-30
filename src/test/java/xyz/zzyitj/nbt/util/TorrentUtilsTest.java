package xyz.zzyitj.nbt.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/5/30 5:45 下午
 * @since 1.0
 */
class TorrentUtilsTest {

    /**
     * 测试根据种子得到torrent信息
     * {@link TorrentUtils#getTorrent(File)}
     * @throws IOException 文件打开错误
     */
    @Test
    void getTorrent() throws IOException {
        File file = new File("/Users/intent/Desktop/sbt/1.torrent");
        System.out.println(TorrentUtils.getTorrent(file));
    }
}
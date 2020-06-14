package xyz.zzyitj.nbt.util;

import xyz.zzyitj.nbt.bean.Torrent;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:50 下午
 * @since 1.0
 */
public class DownloadManagerUtils {
    /**
     * 获取该区块的位置是属于哪个文件
     *
     * @param skipBytes 跳过的字节
     * @return 该区块的位置是属于哪个文件的下标
     */
    public static int getFileIndex(int skipBytes, Torrent torrent) {
        if (torrent.getTorrentFileItemList() == null) {
            return 0;
        }
        int fileIndex = 0;
        long fileLengthSum = 0;
        for (int i = 0; i < torrent.getTorrentFileItemList().size(); i++) {
            fileLengthSum += torrent.getTorrentFileItemList().get(i).getLength();
            if (fileLengthSum > skipBytes) {
                fileIndex = i;
                break;
            }
        }
        return fileIndex;
    }

    /**
     * skipBytes在该文件是第几个字节开始
     *
     * @param skipBytes 跳过的字节
     * @param torrent   种子文件
     * @return 字节开始的位置
     */
    public static long getStartPosition(int skipBytes, Torrent torrent) {
        if (torrent.getTorrentFileItemList() == null || skipBytes == 0) {
            return 0;
        }
        for (int i = 0; i < torrent.getTorrentFileItemList().size(); i++) {
            if (skipBytes - torrent.getTorrentFileItemList().get(i).getLength() < 0) {
                return skipBytes;
            }
            skipBytes -= torrent.getTorrentFileItemList().get(i).getLength();
        }
        return skipBytes;
    }
}

package xyz.zzyitj.nbt.bean;

import java.util.Comparator;

/**
 * xyz.zzyitj.nbt.bean
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/1 12:49 下午
 * @since 1.0
 */
public class TorrentFileItem implements Comparator<String> {
    private String path;
    private long length;

    public TorrentFileItem() {
    }

    public TorrentFileItem(String path, long length) {
        this.path = path;
        this.length = length;
    }

    @Override
    public String toString() {
        return "TorrentFile{" +
                "path='" + path + '\'' +
                ", length=" + length +
                '}';
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
}

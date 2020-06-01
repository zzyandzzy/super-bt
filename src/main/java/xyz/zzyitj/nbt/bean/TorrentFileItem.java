package xyz.zzyitj.nbt.bean;

/**
 * xyz.zzyitj.nbt.bean
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/1 12:49 下午
 * @since 1.0
 */
public class TorrentFileItem {
    private String path;
    private int length;

    public TorrentFileItem() {
    }

    public TorrentFileItem(String path, int length) {
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

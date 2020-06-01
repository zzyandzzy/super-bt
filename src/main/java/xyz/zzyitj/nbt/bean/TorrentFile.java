package xyz.zzyitj.nbt.bean;

/**
 * xyz.zzyitj.nbt.bean
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/1 12:49 下午
 * @since 1.0
 */
public class TorrentFile {
    private String path;
    private Long length;

    public TorrentFile() {
    }

    public TorrentFile(String path, Long length) {
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

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}

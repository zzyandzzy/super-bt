package xyz.zzyitj.nbt.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author intent
 * @date 2019/8/1 16:18
 * @about <link href='http://zzyitj.xyz/'/>
 */
public class Torrent implements Serializable {

    private static final long serialVersionUID = -3617303636715796178L;
    private String announce;
    private byte[] infoHash;
    private Long torrentSize;
    private Long torrentCount;

    @Override
    public String toString() {
        return "Torrent{" +
                "announce='" + announce + '\'' +
                ", infoHash=" + Arrays.toString(infoHash) +
                ", torrentSize=" + torrentSize +
                ", torrentCount=" + torrentCount +
                '}';
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(byte[] infoHash) {
        this.infoHash = infoHash;
    }

    public Long getTorrentSize() {
        return torrentSize;
    }

    public void setTorrentSize(Long torrentSize) {
        this.torrentSize = torrentSize;
    }

    public Long getTorrentCount() {
        return torrentCount;
    }

    public void setTorrentCount(Long torrentCount) {
        this.torrentCount = torrentCount;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }
}

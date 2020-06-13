package xyz.zzyitj.nbt.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author intent
 * @date 2019/8/1 16:18
 * @about <link href='http://zzyitj.xyz/'/>
 */
public class Torrent implements Serializable {

    private static final long serialVersionUID = -3617303636715796178L;
    private String announce;
    /**
     * <a href="https://www.bittorrent.org/beps/bep_0012.html"></a>
     */
    private List<String> announceList;
    private long creationDate;
    private String createdBy;
    private String comment;
    private String encoding;
    private byte[] infoHash;
    private int pieceLength;
    private byte[] pieces;
    /**
     * 是否是私有种子
     * 私有种子不在DHT网络获取peer
     * 只在tracker上获取peer
     * <a href="https://www.bittorrent.org/beps/bep_0027.html"></a>
     */
    private boolean isPrivate;
    /**
     * 单文件是种子名称
     * 多文件是种子所在目录即根目录的名称
     */
    private String name;
    /**
     * 种子总大小
     */
    private long torrentLength;
    /**
     * 多文件文件路径以及大小
     */
    private List<TorrentFileItem> torrentFileItemList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Torrent torrent = (Torrent) o;
        return Arrays.equals(infoHash, torrent.infoHash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(infoHash);
    }

    @Override
    public String toString() {
        return "Torrent{" +
                "announce='" + announce + '\'' +
                ", announceList=" + announceList +
                ", creationDate=" + creationDate +
                ", createdBy='" + createdBy + '\'' +
                ", comment='" + comment + '\'' +
                ", encoding='" + encoding + '\'' +
                ", infoHash=" + Arrays.toString(infoHash) +
                ", pieceLength=" + pieceLength +
                ", pieces=" + Arrays.toString(pieces) +
                ", isPrivate=" + isPrivate +
                ", name='" + name + '\'' +
                ", torrentLength=" + torrentLength +
                ", torrentFileList=" + torrentFileItemList +
                '}';
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(byte[] infoHash) {
        this.infoHash = infoHash;
    }

    public long getTorrentLength() {
        return torrentLength;
    }

    public void setTorrentLength(long torrentLength) {
        this.torrentLength = torrentLength;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public void setPieceLength(int pieceLength) {
        this.pieceLength = pieceLength;
    }

    public byte[] getPieces() {
        return pieces;
    }

    public void setPieces(byte[] pieces) {
        this.pieces = pieces;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<String> getAnnounceList() {
        return announceList;
    }

    public void setAnnounceList(List<String> announceList) {
        this.announceList = announceList;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<TorrentFileItem> getTorrentFileItemList() {
        return torrentFileItemList;
    }

    public void setTorrentFileItemList(List<TorrentFileItem> torrentFileItemList) {
        this.torrentFileItemList = torrentFileItemList;
    }
}

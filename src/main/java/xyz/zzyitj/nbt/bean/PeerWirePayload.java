package xyz.zzyitj.nbt.bean;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/18 10:04 上午
 * @email zzy.main@gmail.com
 */
public class PeerWirePayload {
    private int index;
    private int begin;
    private byte[] block;
    private int length;

    public PeerWirePayload() {
    }

    public PeerWirePayload(int index, int begin, byte[] block) {
        this.index = index;
        this.begin = begin;
        this.block = block;
    }

    public PeerWirePayload(int index, int begin, int length) {
        this.index = index;
        this.begin = begin;
        this.length = length;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public byte[] getBlock() {
        return block;
    }

    public void setBlock(byte[] block) {
        this.block = block;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

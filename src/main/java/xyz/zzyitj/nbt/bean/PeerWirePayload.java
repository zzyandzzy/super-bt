package xyz.zzyitj.nbt.bean;

import io.netty.buffer.ByteBuf;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/18 10:04 上午
 * @email zzy.main@gmail.com
 */
public class PeerWirePayload {
    private int index;
    private int begin;
    private ByteBuf block;

    public PeerWirePayload() {
    }

    public PeerWirePayload(int index, int begin, ByteBuf block) {
        this.index = index;
        this.begin = begin;
        this.block = block;
    }

    @Override
    public String toString() {
        return "PeerWirePayload{" +
                "index=" + index +
                ", begin=" + begin +
                ", blockLength=" + block.readableBytes() +
                '}';
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

    public ByteBuf getBlock() {
        return block;
    }

    public void setBlock(ByteBuf block) {
        this.block = block;
    }
}

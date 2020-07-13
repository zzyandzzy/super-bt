package xyz.zzyitj.nbt.bean;

/**
 * xyz.zzyitj.nbt.bean
 * 请求区块下载信息
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/10 8:58 上午
 * @since 1.0
 */
public class RequestPiece {
    /**
     * 指定从零开始的区块索引
     */
    private int index;
    /**
     * 指定在区块中从零开始偏移的 字节
     */
    private int begin;
    /**
     * 指定请求的长度
     */
    private int length;

    @Override
    public String toString() {
        return "RequestPiece{" +
                "index=" + index +
                ", begin=" + begin +
                ", length=" + length +
                '}';
    }

    public RequestPiece(int index, int begin, int length) {
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

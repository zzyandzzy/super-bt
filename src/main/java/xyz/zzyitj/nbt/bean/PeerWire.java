package xyz.zzyitj.nbt.bean;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/18 10:01 上午
 * @email zzy.main@gmail.com
 */
public class PeerWire<T> {
    protected int id;
    protected int size;
    protected T payload;

    public PeerWire() {
    }

    public PeerWire(int id, int size, T payload) {
        this.id = id;
        this.size = size;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}

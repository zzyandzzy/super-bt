package xyz.zzyitj.nbt.bean;

import java.util.Arrays;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/18 10:01 上午
 * @email zzy.main@gmail.com
 */
public class PeerWire {
    private byte id;
    private int size;
    private Object payload;

    public PeerWire() {
    }

    public PeerWire(byte id, int size, Object payload) {
        this.id = id;
        this.size = size;
        this.payload = payload;
    }

    @Override
    public String toString() {
        if (payload instanceof byte[]) {
            return "PeerWire{" +
                    "id=" + id +
                    ", size=" + size +
                    ", payload=" + Arrays.toString((byte[]) payload) +
                    '}';
        }
        return "PeerWire{" +
                "id=" + id +
                ", size=" + size +
                ", payload=" + payload +
                '}';
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Object getPayload() {
        return payload;
    }

    public byte[] getPayloadAsBytes() {
        return (byte[]) payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}

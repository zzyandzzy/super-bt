package xyz.zzyitj.nbt.util;

/**
 * @author intent
 * @version 1.0
 * @date 2020/3/17 8:33 下午
 * @email zzy.main@gmail.com
 */
public class Const {
    /**
     * 服务端口
     * <p>
     * the server default port
     */
    public static final int SERVER_PORT = 11280;
    /**
     * 超时时间
     * <p>
     * timeout with connect the ip:port
     */
    public static final int TIMEOUT = 5000;

    /**
     * Transmission 2.84的PeerID
     * 更多客服端查看<a href="http://bittorrent.org/beps/bep_0020.html"></a>
     * <p>
     * Transmission 2.84 peer id.
     * more peer id find by <a href="http://bittorrent.org/beps/bep_0020.html"></a>
     */
    public static final byte[] TEST_PEER_ID = {
            45, 84, 82, 50, 56, 52, 48, 45, 99, 120, 105, 98, 115, 110, 53, 100, 104, 114, 102, 101
    };
}

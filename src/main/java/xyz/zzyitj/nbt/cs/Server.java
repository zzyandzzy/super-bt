package xyz.zzyitj.nbt.cs;

/**
 * xyz.zzyitj.nbt.server
 * BT服务端的基本方法
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 9:03 上午
 * @since 1.0
 */
public interface Server {
    /**
     * 开始连接目标
     * start connect target.
     * @throws InterruptedException 连接异常 connection exception
     */
    void start() throws InterruptedException;
}

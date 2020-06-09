package xyz.zzyitj.nbt.client;

/**
 * xyz.zzyitj.nbt.client
 * BT客服端的基本方法
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 8:21 上午
 * @since 1.0
 */
public interface Client {
    /**
     * 开始连接目标
     *
     * @throws InterruptedException 连接异常
     */
    void start() throws InterruptedException;

}

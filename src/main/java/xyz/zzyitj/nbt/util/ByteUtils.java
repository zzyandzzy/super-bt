package xyz.zzyitj.nbt.util;

import org.apache.commons.lang3.StringUtils;

/**
 * xyz.zzyitj.nbt.util
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/5/31 11:08 上午
 * @since 1.0
 */
public class ByteUtils {
    /**
     * 把16进制的data[start,end]转换为十进制int
     *
     * @param data  字节数组
     * @param start 开始的位置
     * @param end   结束的位置
     * @return 10进制
     */
    public static int bytesToInt(byte[] data, int start, int end) {
        int num = 0;
        for (int i = end; i >= start; i--) {
            // 先把当前的data[i]求出，再求和
            int temp = data[i] << (8 * (end - i));
            num += temp;
        }
        return num;
    }
}

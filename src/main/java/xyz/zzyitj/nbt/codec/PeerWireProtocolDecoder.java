package xyz.zzyitj.nbt.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import xyz.zzyitj.nbt.util.HandshakeUtils;
import xyz.zzyitj.nbt.util.PeerWireConst;

import java.nio.ByteOrder;

/**
 * xyz.zzyitj.nbt.client
 * 解码PeerWire协议包
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/2 9:09 上午
 * @since 1.0
 */
public class PeerWireProtocolDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * @param maxFrameLength      帧的最大长度
     * @param lengthFieldOffset   length字段偏移的地址
     * @param lengthFieldLength   length字段所占的字节长
     * @param lengthAdjustment    修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
     * @param initialBytesToStrip 解析时候跳过多少个长度
     * @param failFast            为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
     */
    public PeerWireProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                                   int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    /**
     * 处理特殊的帧
     *
     * @param buf    帧
     * @param offset 偏移
     * @param length length字段所占的字节长
     * @param order  大端还是小端存储，默认机器是啥字节存储它就是啥
     * @return 要分割的字节长度 - length字段所占的字节长
     */
    @Override
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
        // 这里读取第一个字节来判断是否是第一次收到包
        // 第一次收到包的头是没有字节长度是（因为固定了为68个字节）
        // 而且第一个字节肯定是0x13，即BT协议1.0版本的标志位
        // 而如果不是第一次收到包，第一位也不可能是0x13
        // 因为BT协议规定了一个包的长度最大为16KB，即长度为16348
        // 16348的16进制是0x00003FDC，可以看到第一位为0，所以不可能是0x13
        byte checkByte = buf.getByte(offset);
        if (checkByte == HandshakeUtils.BIT_TORRENT_PROTOCOL_VERSION_1_0) {
            // 这里因为构造参数设置了前length长度为帧长度，而第一个握手包里面没有包含长度信息（定长68个字节）
            // 所以这里要减去length
            return HandshakeUtils.HANDSHAKE_LENGTH - length;
        } else {
            // 指定大端存储
            return super.getUnadjustedFrameLength(buf, offset, length, ByteOrder.BIG_ENDIAN);
        }
    }

    /**
     * 组合好的帧会传递到这个函数
     *
     * @param ctx ctx
     * @param in  组合好的帧
     * @return 传递到Handler的参数
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//        System.out.printf("Client: %s length: %d\n", ctx.channel().remoteAddress(), in.readableBytes());
        // 在这里调用父类的方法,实现指得到想要的部分
        in = (ByteBuf) super.decode(ctx, in);

        if (in == null) {
//            System.out.printf("Client: %s discard package.\n", ctx.channel().remoteAddress());
            return null;
        }
        if (in.readableBytes() < PeerWireConst.PEER_WIRE_MIN_FRAME_LENGTH) {
            throw new Exception("Client: " + ctx.channel().remoteAddress() + " byte length: " + in.readableBytes() + " error.");
        }
        // 读取body
//        byte[] data = new byte[in.readableBytes()];
//        in.readBytes(data);
        return in;
    }
}

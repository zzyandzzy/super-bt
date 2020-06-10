package xyz.zzyitj.nbt.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import xyz.zzyitj.nbt.bean.*;
import xyz.zzyitj.nbt.util.HandshakeUtils;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * xyz.zzyitj.nbt.client
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/9 6:58 下午
 * @since 1.0
 */
public class TCPClientHandler extends TCPHandler {

    public TCPClientHandler(TCPClientHandlerBuilder builder) {
        super();
        this.savePath = builder.savePath;
        this.torrent = builder.torrent;
    }

    @Override
    void doChock(ChannelHandlerContext ctx) {
        closePeer(ctx);
    }

    @Override
    void doUnChock(ChannelHandlerContext ctx, byte[] data) {
        unChoke = true;
        // 之后就可以请求下载区块了
        doDownload(ctx);
    }

    /**
     * 从第0个块的begin处开始下载
     *
     * @param ctx ctx
     */
    private void doDownload(ChannelHandlerContext ctx) {
        if (requestPieceQueue != null) {
            RequestPiece requestPiece = requestPieceQueue.peek();
            if (requestPiece != null) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(
                        HandshakeUtils.requestPieceHandler(
                                new RequestPiece(requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength()))));
                System.out.printf("Client: request piece, index: %s, begin: %d, length: %d, need: %d\n",
                        requestPiece.getIndex(), requestPiece.getBegin(), requestPiece.getLength(), requestPieceQueue.size());
            }
        }
    }

    @Override
    void doInterested() {
    }

    @Override
    void doNotInterested() {
    }

    @Override
    void doHave() {
    }

    /**
     * 处理BitField请求
     *
     * @param ctx  ctx
     * @param data 字节数组
     */
    @Override
    void doBitField(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        // 根据peer返回的区块完成信息生成区块下载队列
        generateRequestPieceQueue(peerWire);
        if (unChoke) {
            // 之后就可以下载区块了
            doDownload(ctx);
        }
    }

    /**
     * 根据peer返回的区块完成信息生成区块下载队列
     *
     * @param peerWire peerWire
     */
    private void generateRequestPieceQueue(PeerWire peerWire) {
        // 这里直接生成全部下载，后面再修改
        // increment判断是否刚好下载完
        int increment = torrent.getTorrentLength() % HandshakeUtils.PIECE_MAX_LENGTH == 0 ? 0 : 1;
        // 需要下载的次数，即下载队列的大小
        this.pieceRequestSum = torrent.getTorrentLength() / HandshakeUtils.PIECE_MAX_LENGTH + increment;
        if (requestPieceQueue == null) {
            // 确定下载队列大小
            // 为啥是16Kb呢？因为这是bt协议限制的，单次只能下载16Kb
            // 种子内容大小 / 16Kb = 要下载几次
            requestPieceQueue = new LinkedBlockingQueue<>(this.pieceRequestSum);
        }
        // 当前字节数
        int byteSum = 0;
        // 区块数
        int pieceSum = torrent.getPieces().length / 20;
        int pieceRequestSumIncrement = this.pieceRequestSum % pieceSum == 0 ? 0 : 1;
        // 一个区块需要请求的次数
        this.onePieceRequestSum = this.pieceRequestSum / pieceSum + pieceRequestSumIncrement;
        // 当前的位置
        int currentIndex = 0;
        for (int i = 0; i < pieceSum; i++) {
            for (int j = 0; j < this.onePieceRequestSum; j++) {
                int begin = j * HandshakeUtils.PIECE_MAX_LENGTH;
                int length = HandshakeUtils.PIECE_MAX_LENGTH;
                // 判断是否是最后一个下载请求
                // 因为最后一个下载请求的大小很大可能不是16Kb
                if (currentIndex < this.pieceRequestSum - 1) {
                    byteSum += length;
                    requestPieceQueue.offer(new RequestPiece(i, begin, length));
                } else {
                    length = torrent.getTorrentLength() - byteSum;
                    byteSum += length;
                    requestPieceQueue.offer(new RequestPiece(i, begin, length));
                    break;
                }
                currentIndex++;
            }
        }
    }

    @Override
    void doRequest() {
    }

    /**
     * 保存区块内容
     *
     * @param ctx  ctx
     * @param data data
     */
    @Override
    void doPiece(ChannelHandlerContext ctx, byte[] data) {
        PeerWire peerWire = HandshakeUtils.parsePeerWire(data);
        PeerWirePayload peerWirePayload = (PeerWirePayload) peerWire.getPayload();
        saveFile(ctx, peerWirePayload);
    }

    private void saveFile(ChannelHandlerContext ctx, PeerWirePayload peerWirePayload) {
        if (torrent.getTorrentFileItemList() == null) {
            saveSingleFile(ctx, peerWirePayload);
        } else {
            saveMultipleFile(ctx, peerWirePayload);
        }
    }

    private void saveMultipleFile(ChannelHandlerContext ctx, PeerWirePayload peerWirePayload) {
        byte[] block = peerWirePayload.getBlock();
        // 1.判断该区块的位置是属于哪个文件
        int skipBytes = peerWirePayload.getIndex() * this.onePieceRequestSum * HandshakeUtils.PIECE_MAX_LENGTH
                + peerWirePayload.getBegin();
        int fileIndex = getFileIndex(skipBytes);
        TorrentFileItem torrentFileItem = torrent.getTorrentFileItemList().get(fileIndex);
        // 待处理的字节的大小
        int length = 7;
        // 保存文件
        if (block.length > torrentFileItem.getLength()) {
            int subBegin = block.length - length;
            byte[] subBlock = new byte[subBegin];
            System.arraycopy(block, length, subBlock, 0, subBegin);
            saveMultipleFile(ctx, new PeerWirePayload(
                    peerWirePayload.getIndex(), peerWirePayload.getBegin() + subBegin, subBlock));
        }
    }

    /**
     * 获取该区块的位置是属于哪个文件
     *
     * @param skipBytes 跳过的字节
     * @return 该区块的位置是属于哪个文件的下标
     */
    private int getFileIndex(int skipBytes) {
        int fileIndex = 0;
        int fileLengthSum = 0;
        for (int i = 0; i < torrent.getTorrentFileItemList().size(); i++) {
            if (fileLengthSum >= skipBytes) {
                fileIndex = i;
                break;
            } else {
                fileLengthSum += torrent.getTorrentFileItemList().get(i).getLength();
            }
        }
        return fileIndex;
    }

    private void saveSingleFile(ChannelHandlerContext ctx, PeerWirePayload peerWirePayload) {
        try {
            File file = new File(savePath + torrent.getName());
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            // 跳过多少字节
            // index * onePieceRequestSum * 16Kb + begin
            int skipBytes = peerWirePayload.getIndex() * this.onePieceRequestSum * HandshakeUtils.PIECE_MAX_LENGTH
                    + peerWirePayload.getBegin();
            randomAccessFile.skipBytes(skipBytes);
            randomAccessFile.write(peerWirePayload.getBlock());
            randomAccessFile.close();
            requestPieceQueue.poll();
            System.out.printf("Client: response piece, index: %d, begin: %d, length: %d, skipBytes: %d, fileLength: %d, torrentLength: %d\n",
                    peerWirePayload.getIndex(), peerWirePayload.getBegin(), peerWirePayload.getBlock().length,
                    skipBytes, file.length(), torrent.getTorrentLength());
            if (requestPieceQueue != null) {
                // 判断是否要继续下载区块
                if (requestPieceQueue.size() == 0 && file.length() == torrent.getTorrentLength()) {
                    System.out.printf("Client: download %s complete!\n", torrent.getName());
                    closePeer(ctx);
                } else {
                    doDownload(ctx);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void doCancel() {

    }

    @Override
    void doPort() {

    }

    @Override
    void doExtended() {

    }

    public static class TCPClientHandlerBuilder {
        /**
         * 种子信息
         */
        private Torrent torrent;
        /**
         * 文件下载位置
         */
        private String savePath;

        public TCPHandler build() {
            return new TCPClientHandler(this);
        }

        public TCPClientHandlerBuilder torrent(Torrent torrent) {
            this.torrent = torrent;
            return this;
        }

        public TCPClientHandlerBuilder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }
    }
}

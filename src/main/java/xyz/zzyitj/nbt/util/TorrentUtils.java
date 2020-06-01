package xyz.zzyitj.nbt.util;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import org.apache.commons.io.FileUtils;
import xyz.zzyitj.nbt.bean.Torrent;
import xyz.zzyitj.nbt.bean.TorrentFileItem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author intent
 * @date 2019/7/24 12:50
 */
public class TorrentUtils {
    /**
     * 必选
     * 该关键字的值为Tracker的URL。
     */
    private static final String ANNOUNCE_NAME = "announce";
    /**
     * 可选
     * 它的值存放的是备用Tracker的URL。
     */
    private static final String ANNOUNCE_LIST_NAME = "announce-list";
    /**
     * 可选
     * 该关键字对应的值存放的是种子文件创建的时间。
     */
    private static final String CREATION_DATE_NAME = "creation date";
    /**
     * 可选
     * 该关键字对应的值存放的是种子文件创建的时间。
     */
    private static final String CREATED_BY_NAME = "created by";
    /**
     * 可选
     * 它的值存放的是种子文件制作者的备注信息。
     */
    private static final String COMMENT_NAME = "comment";
    /**
     * 指出info中pieces部分的编码类型，一般为UTF-8，有时也会遇到GBK。
     */
    private static final String ENCODING_NAME = "encoding";
    /**
     * 该关键字对应的值是一个字典，它有两种模式，”single file”和”multiple file”：单文件模式和多文件模式。
     * 单文件模式是指共享的文件只有一个，
     * 多文件模式是指提供共享的文件不止一个，而是两个或两个以上，
     * 如果使用BT软件下载一部影片时，影片的上下部分可能分别放在不同的文件里。
     */
    private static final String INFO_NAME = "info";
    /*--------------------单文件和多文件共享参数START--------------------*/
    /**
     * 单文件：共享文件的文件名，也就是要下载的文件的文件名。
     * 多文件：存放共享文件的文件夹名字。
     */
    private static final String FILE_NAME = "name";
    /**
     * 单文件存在于info里面
     * 多文件存在于info->files里面
     * 文件的长度，以byte为单位。
     */
    private static final String LENGTH_NAME = "length";
    /**
     * 单文件存在于info里面
     * 多文件存在于info->files里面
     * 可选，是共享文件的md5值，这个值在bt协议中根本不用。
     */
    private static final String MD5SUM_NAME = "md5sum";
    /*--------------------单文件和多文件共享参数END--------------------*/
    /*--------------------多文件其他参数START--------------------*/
    /**
     * 它的值是一个列表，含有多个字典，每个共享文件为一个字典。
     * 每个字典中含有三个关键字：length、md5sum、path
     */
    private static final String FILES_NAME = "files";
    /**
     * 存放共享文件的路径和文件名。
     */
    private static final String PATH_NAME = "path";
    /*--------------------多文件其他参数END--------------------*/
    /**
     * 一个区块的大小
     */
    private static final String PIECE_LENGTH_NAME = "piece length";
    /**
     * 字符串类型，存放每个piece的hash值，这个字符串长度一定是20的倍数，因为每个piece的hash值的长度为20字节。
     */
    private static final String PIECES_NAME = "pieces";
    /**
     * 该值如果为1，则表明客户端必须通过连接Tracker来获取其他下载者信息，即peer的IP地址和端口号；
     * 如果为0，则表明客户端还可以通过其它方式获取peer的IP地址和端口号，
     * 如DHT方式，DHT(Distribute Hash Tabel)即分布式哈希表，
     * 它是一种以分布式的方式来获取peer的方法，
     * 现在许多BT客户端既支持通过Tracker来获取peer，也支持通过DHT来获取peer，
     * 如果种子文件中没有private关键字，则表明不限制一定要通过连接tracker来获取peer。
     */
    private static final String PRIVATE_NAME = "private";

    /**
     * 根据data字节数组返回torrent
     *
     * @param data 字节数组
     * @return torrent
     */
    public static Torrent getTorrent(byte[] data) {
        Torrent torrent = new Torrent();
        Bencode bencode = new Bencode(true);
        Map<String, Object> torrentMap = bencode.decode(data, Type.DICTIONARY);
        // 获取种子announce
        ByteBuffer announceBuffer = (ByteBuffer) torrentMap.get(ANNOUNCE_NAME);
        if (announceBuffer != null) {
            torrent.setAnnounce(new String(announceBuffer.array()));
        }
        // 获取种子备用announce
        List<String> announceList = (List<String>) torrentMap.get(ANNOUNCE_LIST_NAME);
        if (announceList != null) {
            torrent.setAnnounceList(announceList);
        }
        // 获取种子创建时间
        Long creationDate = (Long) torrentMap.get(CREATION_DATE_NAME);
        if (creationDate != null) {
            torrent.setCreationDate(creationDate);
        }
        // 获取种子创建人
        ByteBuffer createdByBuffer = (ByteBuffer) torrentMap.get(CREATED_BY_NAME);
        if (createdByBuffer != null) {
            torrent.setCreatedBy(new String(createdByBuffer.array()));
        }
        // 获取种子备注
        ByteBuffer commentBuffer = (ByteBuffer) torrentMap.get(COMMENT_NAME);
        if (commentBuffer != null) {
            torrent.setCreatedBy(new String(commentBuffer.array()));
        }
        // 获取种子pieces编码
        ByteBuffer encodingBuffer = (ByteBuffer) torrentMap.get(ENCODING_NAME);
        if (encodingBuffer != null) {
            torrent.setCreatedBy(new String(encodingBuffer.array()));
        }
        // infoMap
        Map<String, Object> infoMap = (Map<String, Object>) torrentMap.get(INFO_NAME);
        // hash
        torrent.setInfoHash(hash(bencode.encode(infoMap)));
        // name
        ByteBuffer nameBuffer = (ByteBuffer) infoMap.get(FILE_NAME);
        torrent.setName(new String(nameBuffer.array()));
        // pieceLength
        Long pieceLength = (Long) infoMap.get(PIECE_LENGTH_NAME);
        torrent.setPieceLength(pieceLength.intValue());
        // pieces
        ByteBuffer piecesBuffer = (ByteBuffer) infoMap.get(PIECES_NAME);
        torrent.setPieces(piecesBuffer.array());
        // private
        Long isPrivate = (Long) infoMap.get(PRIVATE_NAME);
        if (isPrivate != null) {
            torrent.setPrivate(isPrivate == 1L);
        }
        // files
        List<Map<String, Object>> fileList = (List<Map<String, Object>>) infoMap.get(FILES_NAME);
        // 多文件
        if (fileList != null) {
            int torrentLength = 0;
            List<TorrentFileItem> torrentFileItemList = new ArrayList<>();
            for (Map<String, Object> fileListItemMap : fileList) {
                Long length = (Long) fileListItemMap.get(LENGTH_NAME);
                torrentLength += length.intValue();
                List<ByteBuffer> pathList = (List<ByteBuffer>) fileListItemMap.get(PATH_NAME);
                StringBuilder path = new StringBuilder();
                if (pathList.size() > 1) {
                    for (int i = 0; i < pathList.size(); i++) {
                        path.append(new String(pathList.get(i).array()));
                        if (i != pathList.size() - 1) {
                            path.append("/");
                        }
                    }
                } else {
                    path.append(new String(pathList.get(0).array()));
                }
                torrentFileItemList.add(new TorrentFileItem(path.toString(), length.intValue()));
            }
            torrent.setTorrentFileItemList(torrentFileItemList);
            torrent.setTorrentLength(torrentLength);
        } else {
            Long length = (Long) infoMap.get(LENGTH_NAME);
            torrent.setTorrentLength(length.intValue());
        }
        return torrent;
    }

    /**
     * 根据文件获取torrent
     *
     * @param file 种子文件
     * @return Torrent
     * @throws IOException 文件打开异常
     */
    public static Torrent getTorrent(File file) throws IOException {
        byte[] fileBytes = FileUtils.readFileToByteArray(file);
        return getTorrent(fileBytes);
    }

    /**
     * hash摘要算法
     *
     * @param data hash
     * @return 字节数组
     */
    public static byte[] hash(byte[] data) {
        byte[] result = new byte[]{};
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(data);
            result = crypt.digest();
        } catch (NoSuchAlgorithmException e) {
            // 系统没有实现SHA-1加密算法
            e.printStackTrace();
        }
        return result;
    }
}

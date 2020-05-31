package xyz.zzyitj.nbt.util;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import org.apache.commons.io.FileUtils;
import xyz.zzyitj.nbt.bean.Torrent;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author intent
 * @date 2019/7/24 12:50
 */
public class TorrentUtils {
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
        ByteBuffer announceByteBuffer = (ByteBuffer) torrentMap.get("announce");
        if (announceByteBuffer != null) {
            torrent.setAnnounce(new String(announceByteBuffer.array()));
        }
        Map<String, Object> infoMap = (Map<String, Object>) torrentMap.get("info");
        // info
        List<Object> files = (List<Object>) infoMap.get("files");
        // hash
        torrent.setInfoHash(hash(bencode.encode(infoMap)));
        // torrentSize
        long torrentSize = 0;
        long torrentCount = 0;
        if (files != null) {
            for (Object o : files) {
                Map<String, Object> pace = (Map<String, Object>) o;
                torrentSize += (long) pace.get("length");
                List<Object> path = (List<Object>) pace.get("path");
                for (Object value : path) {
                    ByteBuffer name = (ByteBuffer) value;
                    String s = new String(name.array());
                    if (s.contains(".")) {
                        torrentCount++;
                    }
                }
            }
        } else {
            torrentSize = (long) infoMap.get("length");
            torrentCount = 1;
        }
        torrent.setTorrentSize(torrentSize);
        torrent.setTorrentCount(torrentCount);
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

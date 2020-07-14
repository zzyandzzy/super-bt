package xyz.zzyitj.nbt.manager;

import xyz.zzyitj.nbt.Application;
import xyz.zzyitj.nbt.bean.*;

import java.util.Map;

/**
 * xyz.zzyitj.nbt.handler
 *
 * @author intent zzy.main@gmail.com
 * @date 2020/6/12 8:55 下午
 * @since 1.0
 */
public class DownloadManager extends AbstractDownloadManager {
    @Override
    public boolean save(PeerWirePayload payload) {
        if (getTorrent() == null) {
            return false;
        }
        DownloadConfig downloadConfig = Application.downloadConfigMap.get(getTorrent());
        if (downloadConfig == null) {
            return false;
        }
        Map<Integer, RequestPiece> pieceRequestMap = downloadConfig.getPieceRequestMap();
        if (pieceRequestMap == null) {
            return false;
        }
        if (getTorrent().getTorrentFileItemList() == null) {
            return SingletonDownloadManager.getInstance().saveSingleFile(getTorrent(), payload, downloadConfig, pieceRequestMap);
        } else {
            return SingletonDownloadManager.getInstance().saveMultipleFile(getTorrent(), payload, downloadConfig, pieceRequestMap);
        }
    }
}

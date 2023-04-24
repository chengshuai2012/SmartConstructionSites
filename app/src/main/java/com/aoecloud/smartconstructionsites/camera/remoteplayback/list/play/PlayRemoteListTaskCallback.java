/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-17 上午11:43:40
 * @Description: null
 */
package com.aoecloud.smartconstructionsites.camera.remoteplayback.list.play;

import com.videogo.openapi.bean.resp.CloudFile;
import com.videogo.remoteplayback.RemoteFileInfo;

public interface PlayRemoteListTaskCallback {

    void playCloudPasswordError(CloudFile cloudFile);

    void playSucess();

    void playLocalPasswordError(RemoteFileInfo fileInfo);

    void playException(int errorCode, int retryCount, String detail);

    void playTaskOver(int type);

    void capturePictureSuccess(String filePath);

    void capturePictureFail(int errorCode);

    void startRecordSuccess(String filePath);

    void startRecordFail(int errorCode);

}

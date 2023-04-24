
package com.aoecloud.smartconstructionsites.camera.remoteplayback.list.querylist;

import com.aoecloud.smartconstructionsites.camera.remoteplayback.list.bean.CloudPartInfoFileEx;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;


import java.util.List;

public interface QueryPlayBackListTaskCallback {

    void queryHasNoData();

    void queryOnlyHasLocalFile();

    void queryOnlyLocalNoData();

    void queryLocalException();

    void querySuccessFromCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int queryMLocalStatus, List<CloudPartInfoFile> cloudPartInfoFile);

    void querySuccessFromDevice(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int position, List<CloudPartInfoFile> cloudPartInfoFile);

    void queryLocalNoData();

    void queryException();

    void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail);

}

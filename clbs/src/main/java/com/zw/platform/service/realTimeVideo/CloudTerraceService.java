package com.zw.platform.service.realTimeVideo;


import com.zw.platform.domain.realTimeVideo.CloudTerraceForm;


public interface CloudTerraceService {
    void sendParam(CloudTerraceForm form,String ipAddress) throws Exception;
}

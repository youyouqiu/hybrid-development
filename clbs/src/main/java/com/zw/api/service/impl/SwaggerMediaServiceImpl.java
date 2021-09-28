package com.zw.api.service.impl;

import com.zw.api.domain.MediaInfo;
import com.zw.api.repository.mysql.SwaggerMediaDao;
import com.zw.api.service.SwaggerMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SwaggerMediaServiceImpl implements SwaggerMediaService {
    @Autowired
    private SwaggerMediaDao swaggerMediaDao;

    @Value("${fdfs.webServerUrl}")
    private String mediaServerHost;

    @Override
    public List<MediaInfo> listMonitorMedia(String monitorName, LocalDateTime start, LocalDateTime end) {
        final List<MediaInfo> mediaInfos = swaggerMediaDao.listMediaUrls(monitorName, start, end);
        for (MediaInfo info : mediaInfos) {
            info.setUrl(mediaServerHost + info.getUrl());
        }
        return mediaInfos;
    }
}

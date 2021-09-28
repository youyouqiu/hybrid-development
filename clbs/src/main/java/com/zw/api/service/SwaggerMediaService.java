package com.zw.api.service;

import com.zw.api.domain.MediaInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface SwaggerMediaService {
    List<MediaInfo> listMonitorMedia(final String monitorName, final LocalDateTime start, final LocalDateTime end);
}

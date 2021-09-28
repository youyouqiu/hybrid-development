package com.zw.api.repository.mysql;

import com.zw.api.domain.MediaInfo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SwaggerMediaDao {
    List<MediaInfo> listMediaUrls(@Param("monitorName") String monitorName, @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}

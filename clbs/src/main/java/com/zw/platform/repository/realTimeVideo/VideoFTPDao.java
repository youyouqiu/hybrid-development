package com.zw.platform.repository.realTimeVideo;


import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.realTimeVideo.VideoFTPForm;
import com.zw.platform.domain.realTimeVideo.VideoFTPQuery;


public interface VideoFTPDao {

    int insert(VideoFTPForm record);

    List<VideoFTPQuery> getFtpList(@Param("vehicleId") String vehicleId, @Param("channelNumber") Integer channelNumber,
                                   @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<VideoFTPQuery> getFtpByUrl(@Param("vehicleId") String vehicleId, @Param("channelNumber") Integer channelNumber,
                                    @Param("url") String url);

}
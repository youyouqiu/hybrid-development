package com.zw.platform.repository.realTimeVideo;


import com.zw.platform.domain.realTimeVideo.VideoSpecialSetting;

import java.util.List;

public interface VideoSpecialSettingDao {

    /**
     * 修改音视频特殊参数
     *
     * @param videoSpecial
     * @return
     */
    boolean update(VideoSpecialSetting videoSpecial);

    /**
     * 添加音视频特殊参数
     *
     * @param videoSpecial
     * @return
     */
    boolean insert(VideoSpecialSetting videoSpecial);

    /**
     * 获取所有音视频特殊参数
     *
     * @return
     */
    List<VideoSpecialSetting> selectAll();

}
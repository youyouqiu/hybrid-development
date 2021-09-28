package com.zw.platform.service.realTimeVideo.impl;

import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import com.zw.platform.repository.realTimeVideo.VideoSleepSettingDao;
import com.zw.platform.service.realTimeVideo.VideoSleepService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午4:11:36
* 类说明:
*/
@Service
public class VideoSleepServiceImpl implements VideoSleepService {

    @Autowired
    private VideoSleepSettingDao videoSleepSettingDao;

    @Override
    public JsonResultBean getVideoSleep(String vehicleId) throws Exception {
        return new JsonResultBean(videoSleepSettingDao.getVideoSleepByVehicleId(vehicleId));
    }

    @Override
    public void saveVideoSleep(VideoSleepSetting videoSleep) throws Exception {
        /*if (StringUtils.isBlank(videoSleep.getId())) {
            videoSleepSettingDao.saveVideoSleep(videoSleep);
        } else {
            videoSleepSettingDao.updateVideoSleep(videoSleep);
        }*/
        videoSleepSettingDao.saveVideoSleep(videoSleep);
    }

}

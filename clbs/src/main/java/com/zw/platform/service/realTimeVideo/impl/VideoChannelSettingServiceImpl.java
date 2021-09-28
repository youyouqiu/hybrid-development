package com.zw.platform.service.realTimeVideo.impl;


import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.realTimeVideo.VideoChannelSettingService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午3:23:30
* 类说明:
*/
@Service
public class VideoChannelSettingServiceImpl implements VideoChannelSettingService {

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Override
    public JsonResultBean getVideoChannel(String vehicleId) throws Exception {
        List<VideoChannelSetting> re = videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
        re.sort(new Comparator<VideoChannelSetting>() {
            @Override
            public int compare(VideoChannelSetting o1, VideoChannelSetting o2) {
                return o1.getPhysicsChannel().compareTo(o2.getPhysicsChannel());
            }
        });
        return new JsonResultBean(re);
    }

    @Override
    public void deleteVideoChannelSetting(String vehicleId, Integer physicsChannel) throws Exception {
        videoChannelSettingDao.deleteVideoChannelSetting(vehicleId, physicsChannel);
    }

    /**
     * 根据车辆id查询视频通道列表
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @Override
    public List<VideoChannelSetting> getVideoResourceChannel(String vehicleId) throws Exception {
        return videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
    }
}

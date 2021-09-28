package com.zw.platform.service.realTimeVideo.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.realTimeVideo.VideoSpecialSetting;
import com.zw.platform.repository.realTimeVideo.VideoSpecialSettingDao;
import com.zw.platform.service.realTimeVideo.VideoSpecialSettingService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author zhangsq
 * @date 2018/3/22 17:25
 */
@Service
public class VideoSpecialSettingServiceImpl implements VideoSpecialSettingService {

    @Autowired
    private VideoSpecialSettingDao videoSpecialSettingDao;
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    /**
     * 保存音视频特殊参数
     *
     * @param videoSpecialSetting
     * @param ipAddress
     * @throws Exception
     */
    @Override
    public void saveVideoSpecialSetting(VideoSpecialSetting videoSpecialSetting, String ipAddress) throws Exception {
        List<VideoSpecialSetting> videoSpecialSettings = videoSpecialSettingDao.selectAll();
        boolean flag = false;
        if (videoSpecialSettings.size() > 0) {
            VideoSpecialSetting videoSpecial = videoSpecialSettings.get(0);
            videoSpecial.setType(videoSpecialSetting.getType());
            videoSpecial.setFtpStorage(videoSpecialSetting.getFtpStorage());
            videoSpecial.setVideoPlayTime(videoSpecialSetting.getVideoPlayTime());
            videoSpecial.setVideoRequestTime(videoSpecialSetting.getVideoRequestTime());
            videoSpecial.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            videoSpecial.setUpdateDataTime(new Date());
            videoSpecialSettingDao.update(videoSpecial);
            flag = true;
        } else {
            videoSpecialSetting.setCreateDataUsername(SystemHelper.getCurrentUsername());
            videoSpecialSetting.setCreateDataTime(new Date());
            videoSpecialSettingDao.insert(videoSpecialSetting);
            flag = true;
        }
        if (flag) {
            StringBuilder msg = new StringBuilder();
            if (videoSpecialSettings.size() > 0) {
                msg.append("修改音视频特殊参数");
            } else {
                msg.append("新增音视频特殊参数");
            }
            logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "","-","");
        }
    }

    @Override
    public JSONObject getOne() {
        List<VideoSpecialSetting> videoSpecialSettings = videoSpecialSettingDao.selectAll();
        if (videoSpecialSettings.size() > 0) {
            VideoSpecialSetting videoSpecialSetting = videoSpecialSettings.get(0);
            JSONObject object = new JSONObject();
            object.put("videoPlayTime", videoSpecialSetting.getVideoPlayTime());
            object.put("videoRequestTime", videoSpecialSetting.getVideoRequestTime());
            object.put("ftpStorage", videoSpecialSetting.getFtpStorage());
            object.put("type", videoSpecialSetting.getType());
            return object;
        } else {
            return null;
        }
    }
}

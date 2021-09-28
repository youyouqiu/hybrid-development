package com.zw.platform.vo.realTimeVideo;


import java.io.Serializable;
import java.util.List;

import com.zw.platform.domain.realTimeVideo.RecordingSetting;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2018年1月2日 下午4:14:05
* 类说明:
*/
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoParam implements Serializable {

    private VideoSetting videoSetting;

    private List<VideoChannelSetting> videoChannelSettings;

    private List<AlarmParam> alarmParams;

    private VideoSleepSetting videoSleepSetting;

    private RecordingSetting recordingSetting;

}

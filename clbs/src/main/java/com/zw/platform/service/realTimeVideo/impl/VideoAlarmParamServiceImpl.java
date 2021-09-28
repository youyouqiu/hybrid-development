package com.zw.platform.service.realTimeVideo.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.repository.realTimeVideo.RecordingSettingDao;
import com.zw.platform.service.realTimeVideo.VideoAlarmParamService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.vo.realTimeVideo.AlarmParam;
import com.zw.platform.vo.realTimeVideo.VideoAlarmParam;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午4:59:26
* 类说明:
*/
@Service
public class VideoAlarmParamServiceImpl implements VideoAlarmParamService {

    @Autowired
    private RecordingSettingDao recordingSettingDao;

    @Override
    public JsonResultBean getAlarmParam(String vehicleId) throws Exception {
        Map<String, Object> vedioRecording = recordingSettingDao.getVedioRecordingByVehicleId(vehicleId);
        List<Map<String, String>> vedioAlarms = recordingSettingDao.getVedioAlarmsByVehicleId(vehicleId);
        vedioAlarms.forEach(vedioAlarm -> {
            String name = "";
            if (vedioAlarm.get("name").contains("主存储器")) {
                name = "主存储器故障";
            } else if (vedioAlarm.get("name").contains("主存储器")) {
                name = "主存储器故障";
            } else if (vedioAlarm.get("name").contains("异常驾驶行为")) {
                name = "异常驾驶行为";
            } else if (vedioAlarm.get("name").contains("灾备存储器")) {
                name = "灾备存储器故障";
            } else if (vedioAlarm.get("name").contains("视频信号丢失")) {
                name = "视频信号丢失";
            } else if (vedioAlarm.get("name").contains("视频信号遮挡")) {
                name = "视频信号遮挡";
            } else {
                name = vedioAlarm.get("name");
            }
            vedioAlarm.put("name", name);
        });
        //防止vedioRecording为空出现空指针异常
        if (vedioRecording == null) {
            vedioRecording = new HashMap<String, Object>();
        }
        vedioRecording.put("vedioAlarms", vedioAlarms);
        return new JsonResultBean(vedioRecording);
    }

    @Override
    public void saveVideoAlarmParam(VideoAlarmParam videoAlarmParam) throws Exception {
        if (StringUtils.isNotBlank(videoAlarmParam.getAlarmParam())) {
            //List<AlarmParam> list = JSON.parseArray(videoAlarmParam.getAlarmParam(), AlarmParam.class);
            List<AlarmParam> list = new ArrayList<>();
            for (int i = 1; i < 4; i++) {
                AlarmParam a = new AlarmParam();
                a.setParamValue(i + "");
                a.setAlarmPush(i);
                a.setAlarmName("视频信号遮挡");
                a.setIgnore(1);
                list.add(a);
            }
            List<Map<String, Object>> alarmParamSettings = new ArrayList<>();
            list.forEach(alarmParam -> {
                List<String> alarmParamIds = recordingSettingDao.getIdsByAlarmTypeAndName(alarmParam.getAlarmName());
                alarmParamIds.forEach(alarmParamId -> {
                    Map<String, Object> alarmParamSetting = new HashMap<>();
                    alarmParamSetting.put("id", UUID.randomUUID().toString());
                    alarmParamSetting.put("vehicleId", videoAlarmParam.getVehicleId());
                    alarmParamSetting.put("alarmParameterId", alarmParamId);
                    alarmParamSetting.put("paramValue", alarmParam.getParamValue());
                    alarmParamSetting.put("alarmPush", alarmParam.getAlarmPush());
                    alarmParamSetting.put("ignore", alarmParam.getIgnore());
                    alarmParamSettings.add(alarmParamSetting);
                });
            });
            //删除车下的所有报警参数类型然后再添加
            recordingSettingDao.deleteByVehicleId(videoAlarmParam.getVehicleId());
            //批量添加报警参数设置 
            recordingSettingDao.addVedioAlarmParamSettings(alarmParamSettings);
            //添加音视频录像参数
            videoAlarmParam.setId(UUID.randomUUID().toString());
            recordingSettingDao.addVedioRecordingParamSettings(videoAlarmParam);

        }

    }

}

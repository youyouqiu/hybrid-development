package com.zw.platform.domain.basicinfo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 终端型号缓存实体
 */
@Data
public class TerminalTypeRedisInfo implements Serializable {
    /**
     * 照相参数
     */
    private Integer photoParam;

    /**
     * 视频参数
     */
    private Integer videoParam;

    /**
     * 监控配置项
     */
    private Integer optional;

    public static TerminalTypeRedisInfo assembleTerminalTypeRedisInfo(TerminalTypeInfo info) {
        if (info == null) {
            return null;
        }
        TerminalTypeRedisInfo redisInfo = new TerminalTypeRedisInfo();
        Integer[] optionals = new Integer[4];
        //  是否支持语音监听
        if (info.getSupportMonitoringFlag() == 1) {
            optionals[0] = 1;
        } else {
            optionals[0] = 0;
        }
        // 是否支持行驶记录仪
        if (info.getSupportDrivingRecorderFlag() == 1) {
            optionals[1] = 1;
        } else {
            optionals[1] = 0;
        }
        // 终端型号支持视频
        if (info.getSupportVideoFlag() == 1) {
            redisInfo.setVideoParam(info.getChannelNumber());
            optionals[2] = 1;
        } else {
            optionals[2] = 0;
        }
        // 终端型号支持拍照
        if (info.getSupportPhotoFlag() == 1) {
            redisInfo.setPhotoParam(info.getCamerasNumber());
            optionals[3] = 1;
        } else {
            optionals[3] = 0;
        }
        String result = StringUtils.join(optionals);
        if (StringUtils.isNotBlank(result) && !"0000".equals(result)) {
            redisInfo.setOptional(Integer.valueOf(result, 2));
        }
        return redisInfo;
    }
}

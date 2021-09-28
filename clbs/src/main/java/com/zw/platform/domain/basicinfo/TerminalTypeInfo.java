package com.zw.platform.domain.basicinfo;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.List;


/**
 * 终端型号实体
 */
@Data
public class TerminalTypeInfo extends BaseFormBean {
    /**
     * 终端厂商
     */
    @ExcelField(title = "终端厂商")
    private String terminalManufacturer;

    /**
     * 终端型号
     */
    @ExcelField(title = "终端型号")
    private String terminalType;

    /**
     * 是否支持拍照 0:否; 1:是;
     */
    private Integer supportPhotoFlag;

    @ExcelField(title = "是否支持拍照")
    private String supportPhotoFlagStr;

    /**
     * 摄像头个数(1-5)
     */
    @ExcelField(title = "摄像头个数")
    private Integer camerasNumber;

    /**
     * 是否支持行驶记录仪 0:否; 1:是;
     */
    private Integer supportDrivingRecorderFlag;

    @ExcelField(title = "是否支持行驶记录仪")
    private String supportDrivingRecorderFlagStr;

    /**
     * 是否支持监听 0:否; 1:是;
     */
    private Integer supportMonitoringFlag;

    @ExcelField(title = "是否支持监听")
    private String supportMonitoringFlagStr;

    /**
     * 是否支持视频 0:否; 1:是;
     */
    private Integer supportVideoFlag;

    @ExcelField(title = "是否支持视频")
    private String supportVideoFlagStr;


    /**
     * 是否支持主动安全 0:否; 1:是;
     */
    private Integer activeSafety;

    @ExcelField(title = "是否支持主动安全")
    private String activeSafetyStr;

    /**
     * 是否为一体机 0:否; 1:是;
     */
    private Integer allInOne;

    @ExcelField(title = "是否为一体机")
    private String allInOneStr;

    /**
     * 音频格式  是否支持视频选择(是)该字段才有效; 0:ADPCMA; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K; 6:G711a; 7:G711u
     */
    private Integer audioFormat;

    @ExcelField(title = "实时流音频格式")
    private String audioFormatStr;

    /**
     * 0:8khz; 1:22.05khz; 2:44.1khz; 3:48khz
     */
    private Integer samplingRate;

    @ExcelField(title = "实时流采样率")
    private String samplingRateStr;

    /**
     * 0：单声道  1：双声道
     */
    private Integer vocalTract;

    @ExcelField(title = "实时流声道数")
    private String vocalTractStr;

    /**
     * 0:ADPCMA; 1:G726-8K; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K
     */
    private Integer storageAudioFormat;

    @ExcelField(title = "存储流音频格式")
    private String storageAudioFormatStr;

    /**
     * 0:8khz; 1:22.05khz; 2:44.1khz; 3:48khz
     */
    private Integer storageSamplingRate;

    @ExcelField(title = "存储流采样率")
    private String storageSamplingRateStr;

    /**
     * 0：单声道  1：双声道
     */
    private Integer storageVocalTract;

    @ExcelField(title = "存储流声道数")
    private String storageVocalTractStr;

    /**
     * 通道号个数 1-13 是否支持视频 选择(是)该字段才有效;
     */
    @ExcelField(title = "通道号个数")
    private Integer channelNumber;

    /**
     * 是否支持视频选择(是)该字段才有效; 设备通道号参数,逗号分隔;
     */
    private String deviceChannelId;

    /**
     * 设备通道配置信息
     */
    private List<DeviceChannelSettingInfo> deviceChannelSettingInfoList;
}

package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CameraParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String vid;
    private Integer cameraTimerOpenFlag1;
    private Integer cameraTimerOpenFlag2;
    private Integer cameraTimerOpenFlag3;
    private Integer cameraTimerOpenFlag4;
    private Integer cameraTimerOpenFlag5;
    private Integer cameraTimerSaveFlag1;
    private Integer cameraTimerSaveFlag2;
    private Integer cameraTimerSaveFlag3;
    private Integer cameraTimerSaveFlag4;
    private Integer cameraTimerSaveFlag5;
    private Integer timingUnit;
    private Integer timingSpace;
    private Integer cameraDistanceOpenFlag1;
    private Integer cameraDistanceOpenFlag2;
    private Integer cameraDistanceOpenFlag3;
    private Integer cameraDistanceOpenFlag4;
    private Integer cameraDistanceOpenFlag5;
    private Integer cameraDistanceSaveFlag1;
    private Integer cameraDistanceSaveFlag2;
    private Integer cameraDistanceSaveFlag3;
    private Integer cameraDistanceSaveFlag4;
    private Integer cameraDistanceSaveFlag5;
    /**
     * 定时时间间隔
     */
    private Integer distanceUnit;
    /**
     * 定距距离间隔
     */
    private Integer distanceSpace;
    /**
     * 图形质量
     */
    private Integer pictureQuality;
    /**
     * 亮度
     */
    private Integer luminance;
    /**
     * 对比度
     */
    private Integer contrast;
    /**
     * 饱和度
     */
    private Integer saturation;
    /**
     * 色度
     */
    private Integer chroma;
    private Integer videoTactics;//拍照策略 0、定时拍照 1、定距拍照 2、定时和定距拍照
}

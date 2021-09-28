package com.zw.ws.entity.adas;

import lombok.Data;


/**
 * <p> Title:异常驾驶员行为报警参数设置项 <p> 0x65 Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年08月10日 17:30
 */
@Data
public class DriverVehicleWarning {

    private Integer speedThreshold = 30;// 报警判断速度阈值

    private Integer alarmVolume = 6;// 报警提示音量

    private Integer cameraStrategy = 0x00;// 主动拍照策略

    private Integer timingCamera = 3600;// 主动定时拍照时间间隔

    private Integer fixedCamera = 200;// 主动定距拍照距离间隔

    private Integer cameraNum = 3;// 每次主动拍照张数

    private Integer cameraTime = 2;// 每次主动拍照时间间隔

    private Integer cameraResolution = 0x01;// 拍照分辨率

    private Integer videoResolution = 0x01;// 视频录制分辨率

    private Integer alarmEnable = 0x1FF;// 报警使能

    private Integer eventEnable = 0x03;// 事件使能

    private Integer smokingDecideTime = 180;// 吸烟报警判断时间间隔

    private Integer pickUpDecideTime = 120;// 接打手持电话报警判断时间间隔

    private byte[] keep1 = new byte[3];// 预留，以备将来扩展，用与配置非单独报警类型的参数 1
    //// 疲劳报警有效延时时间阈值 取消了
    // private Integer fatigueValidTime = 0xFF;

    private Integer fatigueSpeed = 50;// 疲劳驾驶报警分级速度阈值

    private Integer fatigueVideoTime = 6;// 疲劳驾驶报警前后视频录制时间

    private Integer fatigueCameraNum = 10;// 疲劳驾驶报警拍照张数

    private Float fatigueCameraTime = 2F;// 疲劳驾驶报警拍照间隔时间

    private Integer pickUpSpeed = 50;// 接打电话报警分级速度阈值

    private Integer pickUpVideoTime = 10;// 打电话报警前后视频录制时间

    private Integer pickUpCameraNum = 3;// 接打电话报警拍驾驶员完整面部特征照片张数

    private Float pickUpCameraTime = 2F;// 接打电话报警拍驾驶员完整面部特征照片间隔时间

    private Integer smokingSpeed = 50;// 抽烟报警分级车速阈值

    private Integer smokingVideoTime = 10;// 抽烟报警前后视频录制时间

    private Integer smokingCameraNum = 3;// 抽烟报警拍驾驶员完整面部特征照片张数

    private Float smokingCameraTime = 2F;// 抽烟报警拍驾驶员完整面部特征照片间隔时间

    private Integer attentionSpeed = 50;// 注意力分散报警分级车速阈值

    private Integer attentionVideoTime = 10;// 注意力分散前后视频录制时间

    private Integer attentionCameraNum = 3;// 注意力分散报警拍照张数

    private Float attentionCameraTime = 2F;// 注意力分散报警拍照间隔时间

    private Integer driveDeedSpeed = 50;// 驾驶行为异常分级速度阈值

    private Integer driveDeedVideoTime = 10;// 驾驶行为异常视频录制时间

    private Integer driveDeedCameraNum = 3;// 驾驶行为异常抓拍照片张数

    private Float driveDeedCameraTime = 2F;// 驾驶行为异常拍照间隔

    private Integer driveDeedType = 0x01;// 触发方式

    private Integer closeEyesSpeed = 50;// 闭眼驾驶报警分级速度阈值

    private Integer closeEyesVideoTime = 10;// 闭眼驾驶报警前后视频录制时间

    private Integer closeEyesCameraNum = 3;// 闭眼驾驶报警抓拍照片张数

    private Float closeEyesCameraTime = 2F;// 闭眼驾驶报警拍照间隔

    private Integer yawnSpeed = 50;// 打哈欠\点头报警分级速度阈值

    private Integer yawnVideoTime = 10;// 打哈欠\点头报警前后视频录制时间

    private Integer yawnCameraNum = 3;// 打哈欠\点头报警抓拍照片张数

    private Float yawnCameraTime = 2F;// 打哈欠\点头报警拍照间隔

    private byte[] keep2 = new byte[1];// 预留4

}

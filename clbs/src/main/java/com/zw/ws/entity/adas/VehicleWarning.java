package com.zw.ws.entity.adas;

import lombok.Data;


/**
 * <p> Title:主动安全辅助系统驾驶员不在驾驶位置预警参数设置项 <p> 0x64 Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team:
 * ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年08月10日 17:30
 */
@Data
public class VehicleWarning {

    private Integer speedThreshold = 30;// 报警判断速度阈值

    private Integer alarmVolume = 6;// 报警提示音量

    private Integer cameraStrategy = 0x00;// 主动拍照策略

    private Integer timingCamera = 5;// 主动定时拍照时间间隔

    private Integer fixedCamera = 100;// 主动定距拍照距离间隔

    private Integer cameraNum = 3;// 每次主动拍照张数

    private Integer cameraTime = 2;// 每次主动拍照时间间隔

    private Integer cameraResolution = 0x01;// 拍照分辨率

    private Integer videoResolution = 0x01;// 视频录制分辨率

    private Integer alarmEnable = 0x10FFF;// 报警使能//1ffe10000

    private Integer eventEnable = 0x03;// 事件使能

    private byte[] keep1 = new byte[1];// 预留，以备将来扩展，用与配置非单独报警类型的参数 16

    private Integer obstacleDistance = 30;// 障碍物预警距离阈值

    private Integer obstacleSpeed = 50;// 障碍物预警分级速度阈值

    private Integer obstacleVideoTime = 10;// 障碍物预警前后视频录制时间

    private Integer obstacleCameraNum = 3;// 障碍物预警拍照张数

    private Float obstacleCameraTime = 2F;// 障碍物预警拍照间隔时间

    private Integer laneChangeTime = 60;// 频繁变道预警判断时间段

    private Integer laneChangeNum = 5;// 频繁变道预警判断次数

    private Integer laneChangeSpeed = 50;// 频繁变道预警分级速度阈值

    private Integer laneChangeVideoTime = 10;// 频繁变道预警前后视频录制时间

    private Integer laneChangeCameraNum = 3;// 频繁变道预警拍照片张数

    private Float laneChangeCameraTime = 2F;// 频繁变道预警拍照间隔

    private Integer deviateSpeed = 50;// 车道偏离预警分级速度阈值

    private Integer deviateVideoTime = 10;// 车道偏离预警前后视频录制时间

    private Integer deviateCameraNum = 3;// 车道偏离预警拍照片张数

    private Float deviateCameraTime = 2F;// 车道偏离预警拍照间隔

    private Integer vehicleCollisionTime = 27;// 驾驶员不在驾驶位置预警时间阈值

    private Integer vehicleCollisionSpeed = 50;// 驾驶员不在驾驶位置报警分级速度阈值

    private Integer vehicleCollisionVideoTime = 10;// 驾驶员不在驾驶位置预警前后视频录制时间

    private Integer vehicleCollisionCameraNum = 3;// 驾驶员不在驾驶位置预警拍照片张数

    private Float vehicleCollisionCameraTime = 2F;// 驾驶员不在驾驶位置预警拍照间隔

    private Integer pedestrianCollisionTime = 30;// 行人碰撞预警时间阈值

    private Integer pedestrianCollisionSpeed = 50;// 行人碰撞预警分级速度阈值

    private Integer pedestrianCollisionVideoTime = 10;// 行人碰撞预警前后视频录制时间

    private Integer pedestrianCollisionCameraNum = 10;// 行人碰撞预警拍照片张数

    private Float pedestrianCollisionCameraTime = 2F;// 行人碰撞预警拍照间隔

    private Integer distanceMail = 30;// 车距监控报警距离阈值

    private Integer distanceSpeed = 50;// 车距监控报警分级速度阈值

    private Integer distanceVideoTime = 10;// 车距过近报警前后视频录制时间

    private Integer distanceCameraNum = 3;// 车距过近报警拍照张数

    private Float distanceCameraTime = 2F;// 车距过近报警拍照间隔

    private Integer speedLimitCameraNum = 3;// 道路标志识别拍照张数

    private Integer speedLimitCameraTime = 2;// 道路标志识别拍照间隔

    private byte[] keep2 = new byte[4];// 预留4

}

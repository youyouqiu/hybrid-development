package com.zw.platform.basic.constant;

import com.zw.platform.basic.core.RedisKey;
import lombok.AllArgsConstructor;

/**
 * 未做改变的redis的key的枚举类型
 * @author zhangjuan
 */

@AllArgsConstructor
public enum HistoryRedisKeyEnum implements RedisKeysConvert {

    /**
     * 终端查询应答
     * DEVICE_QUERY_ANSWER{终端编号}
     */
    DEVICE_QUERY_ANSWER("DEVICE_QUERY_ANSWER%s", 0),

    /**
     * OBD原车数据报表表格数据 KEY
     * obdVehicleDataReportFormDataKey_{username}
     */
    OBD_VEHICLE_DATA_REPORT_FORM_DATA_KEY("obdVehicleDataReportFormDataKey_%s", 1),

    /**
     * 监控对象评分缓存 --clbs 只有删除的维护，其他部件在使用
     */
    ORG_MONITOR_SCORE_PATTERN("*%s_monitorScore", 15),

    /**
     * 终端号与监控对象的映射关系缓存 协议端使用
     */
    DEVICE_VEHICLE_INFO("%s-vehicleinfo", 7),

    DEVICE_VEHICLE_INFO_PATTERN("*-vehicleinfo", 7),

    /**
     * 报警类型名称缓存
     * 报警类型_alarmtype
     */
    ALARM_TYPE_INFO("%s_alarmtype", 2),

    /**
     * 外设轮询
     */
    SENSOR_MESSAGE("sensorMessage%s", 2),

    /**
     * 用户状态缓存
     */
    USER_STATE("%s_state", 8),

    /**
     * 导入错误信息缓存 参数：用户Id-模块名称
     */
    IMPORT_ERROR_USER_MODULE("%s_%s", 8),

    /**
     * 插卡司机的车辆关联
     */
    CARD_NUM_PROFESSIONAL_PREFIX("vp_%s", 4),

    /**
     * 插卡司机的车辆关联
     */
    CARD_NUM_PREFIX("vc_%s", 4),

    /**
     * 插卡从业人员信息0801司机照片上来时用
     */
    IC_PROFESSIONAL_INFO("%s_professional", 4),

    /**
     * 最后一次插卡司机
     */
    LAST_DRIVER("last_driver_%s", 4),

    /**
     * 企业下每月新增的车数
     */
    ORG_VEHICLE_NUM("%s_org_vehicle_num", 9),

    /**
     * 协议端使用，绑定的终端信息 第一个参数：绑定标识 第二个参数：绑定终端设备类型
     */
    DEVICE_BIND("%s_%s", 5),

    SUB_ONE_DEVICE_ID("1_%s", 13),

    /**
     * 根据设备ID报警推送设置的redis缓存key
     */
    ALARM_PUSH_SET_DEVICE_ID("3_%s", 13),

    /**
     * 企业下监控对象评分
     */
    ORG_MONITOR_SCORE("%s_monitorScore", 15),

    /**
     * 根据监控对象ID报警推送设置的redis缓存key
     */
    ALARM_PUSH_SET_MONITOR_ID("4_%s", 13),

    /**
     * 监控对象io报警设置
     */
    MONITOR_IO_ALARM_SETTING("%s_ioAlarm_Setting", 13),

    /**
     * 联动处理
     */
    LINKPAGE_TO_PLATFORM("linkpage_to_platform", 12),

    /**
     * 监控对象状态缓存 参数：监控对象id f3维护的缓存
     */
    MONITOR_STATUS("%s-vehiclestatus", 0),

    /**
     * 模糊监控对象状态缓存 参数：监控对象id f3维护的缓存
     */
    MONITOR_STATUS_FUZZY("*-vehiclestatus*", 0),

    /**
     * 监控对象最后一条位置信息
     */
    MONITOR_LOCATION("%s-location", 7),

    /**
     * 监控对象最后一次报警信息
     */
    MONITOR_LAST_ALARM("%s_monitor_alarmInfo", 9),

    MONITOR_SEND_ALARM("%s_sendalarm", 5),

    /**
     * 监控对象、终端以及SIM模糊搜索
     */
    MONITOR_DEVICE_SIM_FUZZY("vehicle_device_simcard_fuzzy_search", 10),

    /**
     * 插卡司机信息
     */
    IC_DRIVER_LIST("zw_ic_driver_list", 15),

    /**
     * 风险编号
     */
    RISK_EVENT_ID("RISK_EVENT:%s", 15),

    USER_REALTIME_MONITORTREE_SET("%s_realTimeMonitorTreeSet", 4),
    /**
     * 图标方向 String
     */
    ICON_DIRECTION("icodirection", 13),
    /**
     * 领导看板数据缓存
     */
    ADAS_REPORT_CACHE("%s_adas_report%s", 11),

    /**
     * 领导看板数据缓存
     */
    ADAS_ORG_VID_CACHE("%d_org_vid_shard_num", 4),

    /**
     * 实时天气情况缓存
     */
    CHINA_WEATHER_LIVE("china_weather_live", 3),

    /**
     * 领导看板地图热点数据缓存
     */
    HOT_MAP_DATA("%d_hot_map_data", 11),
    /**
     * 车辆排行
     */
    ADAS_REPORT_VEHICLE_RANK("%s_adas_report_vehicleRank_%s", 11),
    /**
     * 企业排行
     */
    ADAS_REPORT_GROUP_RANK("%_adas_report_groupRank_%s", 11),
    /**
     * 主动安全参数设置
     */
    ADAS_PARAM_VEHICLE("adas_param_%s", 11),

    /**
     * 标识主动安全的报警是否有设置
     */
    ADAS_RISK_AUTO_VEHICLE("RISK_%s_AUTO", 15),

    /**
     * 监控对象ID_报警ID
     */
    MONITOR_ID_ALARM_ID("%s", 11),

    /**
     * 模糊搜索车辆状态缓存
     */
    FUZZY_VEHICLE_STATUS("*-vehiclestatus*", 0),

    /**
     * 模糊搜索主动安全设置平台提醒的缓存
     */
    FUZZY_ADAS_PLATFORM_REMIND("*%s_platformRemind", 11),

    /**
     * 主动安全参数
     */
    ADAS_PARAM_DEFAULT_PROTOCOL("adas_param_default_%s", 11),

    /**
     * 昨日风险数量
     */
    ADAS_REPORT_YESTERDAY_RISK_NUM("%s_adas_report_%s_YesterdayRiskNum", 11),
    /**
     * 领导看板环比信息
     */
    ADAS_REPORT_RING_RATIO_INFO("%s_adas_report_%s_RingRatioInfo", 11),
    /**
     * 车辆在线率
     */
    ADAS_REPORT_ONLINE_RATE("%s_adas_report_%s_OnLineRate", 11),
    /**
     * 在线客服数量
     */
    ADAS_REPORT_ONLINE_CUSTOMER_SERVICE("online_customer_service_%s", 2),
    /**
     * 用户登录之后，并且是风控人员需要生成一个这样的缓存
     */
    ADAS_REPORT_CUSTOMER_SERVICE("customer_service_%s", 2),

    /**
     * 设置主动安全手动下发9208过期失效的缓存key
     */
    ADAS_MANUAL_SEND_9208_EXPIRE_TIME("%s_subcibeTableKeys", 3),

    /**
     * 监控对象报警参数
     */
    MONITOR_ALARM_PARAM("%s_%s_alarm_param", 13),

    /**
     * 809管理--809平台数据交互管理--平台考核
     */
    USER_PLATFORM_CHECK_LIST("%s_platformCheck_list", 2),

    /**
     * 809管理--809平台数据交互管理--企业考核数据
     */
    USER_CORP_CHECK_INFO("%s_corpCheckInfo_list", 2),

    /**
     * 809管理--809平台数据交互管理--企业车辆违规考核
     */
    USER_CORP_ALARM_CHECK_INFO("%s_corpAlarmCheckInfo_list", 2),

    /**
     * 音视频报警默认推送方式
     */
    DEFAULT_VIDEO_PUSH("defaultVideo_push", 13),

    /**
     * 普通报警默认推送方式
     */
    DEFAULT_PUSH("default_push", 13),

    /**
     * 即将到期的保险单id(存储方式不一样)
     */
    EXPIRE_INSURANCE_ID("expireInsuranceId_list", 10),

    /**
     * 未处理报警车辆缓存
     */
    UNHANDLED_VEHICLE("unhandled-vehicle", 5),

    /**
     * 主动安全智能拍照缓存
     */
    INTELLIGENCE_PHOTO_PARAM_SETTING("%s_intelligence_photoParam_setting", 11),

    /**
     * 主动安全风险处理锁key
     */
    RISK_LOCK("risk_lock_%s", 2),

    /**
     * 系统管理 油补转发管理 转发车辆管理 --下载转发车辆key
     */
    OIL_DOWNLOAD_KEY("%s_oil_download_key", 4),

    /**
     * 系统管理 油补转发管理 上报里程统计
     */
    USER_OIL_SUBSIDY_VEHICLE_MILE_MONTH("%s_oilSubsidyVehicleMileMonthList", 2),

    /**
     * 系统管理 油补转发管理 定位信息统计
     */
    USER_OIL_SUBSIDY_LOCATION_INFORMATION("%s_oilSubsidyLocationInformationList", 2),

    /**
     * 视频轮播 用户的视频设置（未存数据库  redis做临时存储）
     */
    USER_VIDEO_SETTING("%s_videoSetting", 2),

    /**
     * 音视频磁盘信息
     */
    VIDEO_DISKINFO("video_diskInfo", 5),

    /**
     * 轨迹回放--报警数据
     */
    TRACK_PLAYBACK_ALARM_SUFFIX_KEY("%s&%s_track_playback_ALARM_list", 11),
    /**
     * 两客一危轨迹回放
     * 报警数据
     */
    LKYW_TRACK_PLAYBACK_ALARM_SUFFIX_KEY("%s&%s_lkyw_track_playback_ALARM_list", 11),

    /**
     * 定时定区域
     */
    TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY("%s_track_playback_timeZone_list", 10),

    /**
     * 两客一危-定时定区域
     */
    LKYW_TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY("%s_lkyw_track_playback_timeZone_list", 10),
    /**
     * 轨迹回放基础数据key
     * TRACK_PLAYBACK_BASE:{userName}:{monitorId}
     */
    TRACK_PLAYBACK_BASE_DATA("TRACK_PLAYBACK_BASE:%s:%s", 11),

    /**
     * 两客一危--轨迹回放基础数据key
     */
    LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY("%s&%s_lkyw_track_playback_list", 11),

    /**
     * 轨迹回放OBD数据
     * TRACK_PLAYBACK_OBD:{userName}:{monitorId}
     */
    TRACK_PLAYBACK_OBD_DATA("TRACK_PLAYBACK_OBD:%s:%s", 11),

    LKYW_TRACK_PLAYBACK_OBD_SUFFIX_KEY("%s&%s_lkyw_track_playback_OBD_list", 11),

    /**
     * io报警列表缓存
     * {用户uuid}_ioAlarmDeal_list
     */
    USER_IO_ALARM_DEAL_LIST("%s_ioAlarmDeal_list", 11),

    /**
     * 调度报警列表缓存
     * {用户uuid}_dispatchAlarmDeal_list
     */
    USER_DISPATCH_ALARM_DEAL_LIST("%s_dispatchAlarmDeal_list", 11),

    /**
     * 809转发报警列表缓存
     * {用户uuid}_forward809Alarm_list
     */
    USER_FORWARD_809_ALARM_LIST("%s_forward809Alarm_list", 11),

    /**
     * 报警信息报表
     * {用户uuid}_exportInformation_list
     */
    USER_EXPORT_INFORMATION_LIST("%s_exportInformation_list", 11),

    /**
     * 终端里程统计
     * {用户uuid}_terminal_mileage_statistics_list
     */
    USER_TERMINAL_MILEAGE_STATISTICS_LIST("%s_terminal_mileage_statistics_list", 11),

    /**
     * 终端里程明细
     * {用户uuid}_terminal_mileage_daily_detail_list
     */
    USER_TERMINAL_MILEAGE_DAILY_DETAIL_LIST("%s_terminal_mileage_daily_detail_list", 11),

    /**
     * 行驶里程统计 分组总里程
     * {用户uuid}_driving_mileage_statistics_assign_mileage_data
     */
    USER_DRIVING_MILEAGE_STATISTICS_ASSIGN_MILEAGE_DATA("%s_driving_mileage_statistics_assign_mileage_data", 11),
    /**
     * 行驶里程明细
     * {用户uuid}_driving_mileage_details_list
     */
    USER_DRIVING_MILEAGE_DETAILS_LIST("%s_driving_mileage_details_list", 11),
    /**
     * 行驶里程位置明细
     * {用户uuid}_driving_mileage_location_details_list
     */
    USER_DRIVING_MILEAGE_LOCATION_DETAILS_LIST("%s_driving_mileage_location_details_list", 11),
    /**
     * 停驶数据
     * {用户uuid}_stop_data_list
     */
    USER_STOP_DATA_LIST("%s_stop_data_list", 11),
    /**
     * 离线查询报表
     * {用户uuid}_off_line_report_info_list
     */
    USER_OFF_LINE_REPORT_INFO_LIST("%s_off_line_report_info_list", 11),
    /**
     * 连续性分析报表
     * {用户uuid}_continuity_analysis_list
     */
    USER_CONTINUITY_ANALYSIS_LIST("%s_continuity_analysis_list", 11),
    /**
     * 最新位置信息报表
     * {用户uuid}_latest_location_info_list
     */
    USER_LATEST_LOCATION_INFO_LIST("%s_latest_location_info_list", 11),
    /**
     * 出区划累计时长统计列表
     * {用户uuid}_out_area_duration_statistics_list
     */
    USER_OUT_AREA_DURATION_STATISTICS_LIST("%s_out_area_duration_statistics_list", 11),

    /**
     * OBD管理设置
     */
    OBD_SETTING_MONITORY_LIST("vehicle_obd_monitory_list", 10),

    /**
     * obd 故障码导出查询
     * exportFaultCodeList{username}
     */
    OBD_EXPORT_FAULT_CODE_LIST("exportFaultCodeList%s", 12),

    /**
     * 服务到期报表导出查询
     * exportLifecycleList{username}
     */
    EXPORT_LIFECYCLE_LIST("exportLifecycleList%s", 12),

    /**
     * 服务到期的车辆 list
     */
    LIFECYCLE_EXPIRE_LIST("lifecycleExpire_list", 12),

    /**
     * 服务到期的车辆 json字符串
     */
    LIFECYCLE_EXPIRE_STRING("lifecycleExpire_list", 10),

    /**
     * 自定义列
     */
    AlERT_WINDOW_REALTIME_DATA_LIST("AlERT_WINDOW_REALTIME_DATA_LIST", 3),

    USER_AlERT_WINDOW_REALTIME_DATA_LIST("%s_AlERT_WINDOW_REALTIME_DATA_LIST", 3),

    MULTI_WINDOW_REALTIME_DATA_LIST("MULTI_WINDOW_REALTIME_DATA_LIST", 3),

    USER_MULTI_WINDOW_REALTIME_DATA_LIST("%s_MULTI_WINDOW_REALTIME_DATA_LIST", 3),

    REALTIME_MONITORING_ACTIVE_SAFETY_LIST("REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 3),

    USER_REALTIME_MONITORING_ACTIVE_SAFETY_LIST("%s_REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 3),

    REALTIME_MONITORING_ALARM_LIST("REALTIME_MONITORING_ALARM_LIST", 3),

    USER_REALTIME_MONITORING_ALARM_LIST("%s_REALTIME_MONITORING_ALARM_LIST", 3),

    REALTIME_MONITORING_LIST("REALTIME_MONITORING_LIST", 3),

    USER_REALTIME_MONITORING_LIST("%s_REALTIME_MONITORING_LIST", 3),

    REALTIME_MONITORING_LOG_LIST("REALTIME_MONITORING_LOG_LIST", 3),

    USER_REALTIME_MONITORING_LOG_LIST("%s_REALTIME_MONITORING_LOG_LIST", 3),

    REALTIME_MONITORING_OBD_LIST("REALTIME_MONITORING_OBD_LIST", 3),

    USER_REALTIME_MONITORING_OBD_LIST("%s_REALTIME_MONITORING_OBD_LIST", 3),

    TGAC_AlERT_WINDOW_REALTIME_DATA_LIST("TGAC_AlERT_WINDOW_REALTIME_DATA_LIST", 3),

    USER_TGAC_AlERT_WINDOW_REALTIME_DATA_LIST("%s_TGAC_AlERT_WINDOW_REALTIME_DATA_LIST", 3),

    TGAC_RAPID_SCREENING("TGAC_RAPID_SCREENING", 3),

    USER_TGAC_RAPID_SCREENING("%s_TGAC_RAPID_SCREENING", 3),

    TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST("TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 3),

    USER_TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST("%s_TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 3),

    TGAC_REALTIME_MONITORING_ALARM_LIST("TGAC_REALTIME_MONITORING_ALARM_LIST", 3),

    USER_TGAC_REALTIME_MONITORING_ALARM_LIST("%s_TGAC_REALTIME_MONITORING_ALARM_LIST", 3),

    TGAC_REALTIME_MONITORING_LIST("TGAC_REALTIME_MONITORING_LIST", 3),

    USER_TGAC_REALTIME_MONITORING_LIST("%s_TGAC_REALTIME_MONITORING_LIST", 3),

    TRACKPLAY_ALARM("TRACKPLAY_ALARM", 3),

    USER_TRACKPLAY_ALARM("%s_TRACKPLAY_ALARM", 3),

    TRACKPLAY_DATA("TRACKPLAY_DATA", 3),

    USER_TRACKPLAY_DATA("%s_TRACKPLAY_DATA", 3),

    TRACKPLAY_OBD_LIST("TRACKPLAY_OBD_LIST", 3),

    USER_TRACKPLAY_OBD_LIST("%s_TRACKPLAY_OBD_LIST", 3),

    TRACKPLAY_RUN("TRACKPLAY_RUN", 3),

    USER_TRACKPLAY_RUN("%s_TRACKPLAY_RUN", 3),

    TRACKPLAY_SPEED("TRACKPLAY_SPEED", 3),

    USER_TRACKPLAY_SPEED("%s_TRACKPLAY_SPEED", 3),

    TRACKPLAY_STOP("TRACKPLAY_STOP", 3),

    USER_TRACKPLAY_STOP("%s_TRACKPLAY_STOP", 3),

    /**
     * 服务即将到期 0
     */
    EXPIRE_LIFE_CYCLE_REMIND("lifecycleExpire_remind_list", 10),
    /**
     * 服务已经到期 1
     */
    ALREADY_EXPIRE_LIFE_CYCLE("already_lifecycleExpire_list", 10),
    /**
     * 驾驶证即将到期 2
     */
    EXPIRE_DRIVING_LICENSE("expireDrivingLicense_list", 10),
    /**
     * 驾驶证已经到期  3
     */
    ALREADY_EXPIRE_DRIVING_LICENSE("alreadyExpireDrivingLicense_list", 10),
    /**
     * 道路运输证即将到期 4
     */
    EXPIRE_ROAD_TRANSPORT("expireRoadTransport_list", 10),
    /**
     * 道路运输证已经到期 5
     */
    ALREADY_EXPIRE_ROAD_TRANSPORT("alreadyExpireRoadTransport_list", 10),
    /**
     * 保养即将到期 6
     */
    EXPIRE_MAINTENANCE("expireMaintenance_list", 10),
    /**
     * 保险即将到期 7
     */
    EXPIRE_INSURANCE("expireInsurance_list", 10),

    /**
     * 用户在线缓存
     */
    SERVICE_USER("online:%s", 2),

    /**
     * 车辆最近处理的报警
     */
    MONITOR_RECENTLY_HANDLED_ALARM("recently-handled-alarm:%s:%s", 5),
    /**
     * 未处理的报警
     */
    UNHANDLED_ALARM("unhandled-alarm:%s", 5),
    /**
     * 定时任务依赖的企业下的子企业缓存关系
     */
    GROUP_CHILD_IDS("group_child_ids_%d", 4),

    /**
     * 发到设备的围栏信息信息缓存(监控对象ID，围栏下发设备的hashcode)
     */
    FENCE_SEND("fenceSend_%s_%s", 12),

    /**
     * 所有监控对象位置压缩数据
     */
    ALL_MONITOR_POSITION_DATA_ZIP("zh_lb_vehicle_positional", 7),

    /**
     * 四川监管报表 持续超速统计 企业统计
     */
    USER_GROUP_STATISTICS_DATA("%s_groupStatisticsData_list", 2),

    /**
     * 四川监管报表 持续超速统计 车辆统计
     */
    USER_VEHICLE_STATISTICS_DATA("%s_vehicleStatisticsData_list", 2),

    /**
     * 四川监管报表 持续超速统计 车辆明细
     */
    USER_VEHICLE_DETAILS_DATA("%s_vehicleDetailsData_list", 2),

    /**
     * 驾驶员统计缓存的key
     */
    DRIVER_STATISTICS_INFO_LIST("driverStatistics_info_list_%s", 11),
    /**
     * 违章处置报表缓存的key
     */
    VIOLATION_RECORDS_LIST("violationRecords_list_%s", 11),
    /**
     * 车辆序列号
     */
    VEHICLE_MSGSN("%s-vehiclemsgsn", 0),

    /**
     * 四川监管报表 车辆调度信息统计  车辆调度信息道路运输企业统计月报表
     */
    USER_ENTERPRISE_DISPATCH("%s_enterpriseDispatch_list", 2),

    /**
     * 四川监管报表 车辆调度信息统计  车辆调度信息统计月报表
     */
    USER_VEHICLE_DISPATCH_FORMATION("%s_exportVehicleDispatchformation_list", 2),

    /**
     * 四川监管报表 车辆调度信息统计  车辆调度信息明细表
     */
    USER_VEHICLE_DETAIL_DISPATCH_FORMATION("%s_exportDetailListDispatchformation_list", 2),

    /**
     * 四川监管报表 车辆在线率统计  车辆在线率道路运输企业统计月报表
     */
    USER_ENTERPRISE_MONTH_LIST("%s_enterpriseMonth_list", 2),

    /**
     * 四川监管报表 车辆在线率统计  车辆在线率统计月报表
     */
    USER_VEHICLE_MONTH_LIST("%s_vehicleMonth_list", 2),

    /**
     * 四川监管报表 车辆在线率统计  车辆在线明细表
     */
    USER_VEHICLE_ONLINE_DETAIL("%s_vehicleOnlineDetail_list", 2),

    /**
     * 四川监管报表 车辆里程统计  车辆里程统计表
     */
    USER_VEHICLE_MILE_DETAIL_LIST("%s_vehicleMileDetail_list", 2),

    /**
     * 四川监管报表 车辆里程统计  车辆日里程报表
     */
    USER_VEHICLE_MILE_MONTH_LIST("%s_vehicleMileMonth_list", 2),

    /**
     * 四川监管报表 车辆抽查统计  道路运输企业抽查车辆数量统计表
     */
    GROUP_SPOT_CHECK_VEHICLE_NUMBER_KEY("%s_groupSpotCheckVehicleNumberKey", 2),

    /**
     * 四川监管报表 车辆抽查统计  车辆抽查数量统计表
     */
    VEHICLE_SPOT_CHECK_NUMBER_COUNT_DATA_KEY("%s_vehicleSpotCheckNumberCount", 2),

    /**
     * 四川监管报表 车辆抽查统计  用户抽查车辆数量及百分比统计报表
     */
    USER_SPOT_CHECK_NUMBER_AND_PERCENTAGE_DATA_KEY("%s_userSpotCheckNumberAndPercentage", 2),

    /**
     * 四川监管报表 车辆抽查统计  车辆抽查明细信息
     */
    VEHICLE_SPOT_CHECK_DETAIL_DATA_KEY("%s_vehicleSpotCheckDetail", 2),

    /**
     * 四川监管报表 车辆定位统计  企业车辆定位统计
     */
    VEHICLE_LOCATION_STATISTICS("%s_vehicleLocationStatistics", 2),

    /**
     * 四川监管报表 车辆定位统计  企业车辆定位统计  定位统计明细
     */
    LOCATION_STATISTICS_DETAILS("%s_locationStatisticsDetails", 2),

    /**
     * 四川监管报表 车辆定位统计  企业车辆定位统计  无定位统计明细
     */
    NO_LOCATION_STATISTICS_DETAILS("%s_noLocationStatisticsDetails", 2),

    /**
     * 四川监管报表 车辆定位统计  企业车辆定位统计  定位中断统计明细
     */
    LOCATION_INTERRUPT_STATISTICS_DETAILS("%s_locationInterruptStatisticsDetails", 2),

    /**
     * 四川监管报表 车辆定位统计  企业车辆定位统计  离线位移统计明细
     */
    OFFLINE_DISPLACEMENT_STATISTICS_DETAILS("%s_offlineDisplacementStatisticsDetails", 2),

    /**
     * 四川监管报表 车辆定位统计  车辆月度定位统计
     */
    MONTH_LOCATION_STATISTICS("%s_monthLocationStatistics", 2),

    /**
     * 四川监管报表 车辆定位统计  异常定位统计
     */
    ANOMALY_LOCATION_STATISTICS("%s_anomalyLocationStatistics", 2),

    /**
     * 四川监管报表 车辆定位统计  异常定位统计  异常定位明细
     */
    EXCEPTION_LOCATION_DETAILS("%s_exceptionLocationDetails", 2),

    /**
     * 四川监管报表 视频巡检统计
     */
    VIDEO_CAROUSEL_REPORT("%s_videoCarouselReport", 2),

    /**
     * 四川监管报表 视频巡检统计详情
     */
    VIDEO_CAROUSEL_REPORT_DETAIL("%s_videoCarouselReportDetail", 2),

    /**
     * 部标监管报表  车辆综合信息报表
     */
    VEHICLE_COMPREHENSIVE_INFO("%s_vehicleComprehensiveInfo", 2),

    /**
     * 部标监管报表  超速报表
     */
    VEHICLE_SPEEDING_REPORT("%s_speeding_report_list", 2),

    /**
     * 部标监管报表  客流量报表
     */
    PASSENGER_FLOW_REPORT("%s_passenger_flow_list", 2),

    /**
     * 部标监管报表  809查岗督办报表
     */
    INSPECTION_SUPERVISION_LIST("%s_inspection_Supervision_list", 2),

    /**
     * 部标监管报表  行驶记录仪报表
     */
    DRIVING_RECORD_LIST("%s_drivingRecord", 2),

    /**
     * 部标监管报表  上线率报表
     */
    ONLINE_REPORT("%s_onlineReport", 2),

    /**
     * 部标监管报表  报警信息统计  列表
     */
    ALARM_STATISTICS_LIST("%s_alarmMessageList", 2),

    /**
     * 部标监管报表  报警信息统计  详情
     */
    ALARM_STATISTICS_DETAIL("%s_alarmDetail", 2),

    /**
     * 部标监管报表  报警信息统计  车辆运营状态报表
     */
    VEHICLE_OPERATION_STATUS_REPORT("%s_vehicleOperationStatusReport", 2),

    /**
     * 部标监管报表  报警信息统计  轨迹有效性报表
     */
    TRACK_VALID_REPORT("%s_trackValidReport", 2),

    /**
     * 部标监管报表  停止报表
     */
    USER_VEHICLE_STOP_INFO("%s_exportStopInfo_list", 2),

    /**
     * 山西监管报表 疲劳驾驶报警明细
     */
    SX_TIRED_ALARM_REPORT_INFORMATION("%s_exportTiredAlarmReportInformation_list", 2),

    /**
     * 山西监管报表 疲劳驾驶违章统计
     */
    SX_TIRED_VIOLATION_REPORT_INFORMATION("%s_exportTiredViolationReportInformation_list", 2),

    /**
     * 山西监管报表 超速报警明细
     */
    SX_SPEED_ALARM_REPORT_INFORMATION("%s_exportSxSpeedAlarmReportInformation_list", 2),

    /**
     * 山西监管报表 超速违章统计
     */
    SX_SPEED_VIOLATION_REPORT_INFORMATION("%s_exportSxSpeedViolationReportInformation_list", 2),

    /**
     * 山西监管报表 定位数据合格率
     */
    SX_LOCATION_QUALIFIED_RATE_REPORT_INFORMATION("%s_exportLocationQualifiedRateReports_list", 2),

    /**
     * 山西监管报表 飘逸数据报表
     */
    SX_SHIFT_DATA_REPORT_INFORMATION("%s_exportShiftDataReports_list", 2),

    /**
     * 山西监管报表 凌晨2-5点运行报表
     */
    SX_BEFORE_DAWN_REPORT_INFORMATION("%s_exportBeforeDawnReportInformation_list", 2),

    /**
     * 山西监管报表 异常轨迹报表
     */
    SX_ABNORMAL_TRAJECTORY_REPORT_INFORMATION("%s_exportAbnormalTrajectoryReports_list", 2),

    /**
     * app短信下发
     */
    APP_SMS_SEND_CODE("%s_code", 11),

    /**
     * 下线监控对象 参数：监控对象ID
     */
    MONITOR_OFFLINE("%s-offline", 15),
    /**
     * 长时间下线报警信息
     */
    MONITOR_ALARMING("%s-alarming", 15),

    /**
     * 主动安全ftp上车的证据文件是否创建 参数:时间（yyyyMMdd）
     */
    ADAS_VEHICLE_MEDIADIR_FLAG("%s_adas_vehicle_mediadir_flag", 0),

    /**
     * 传感器报表-温度，参数分别是moId、beginTime（时间戳，秒，下同）、endTime、标记
     */
    STATS_TEMP("TEMP_STATIS:%s-%d-%d%s", 1),

    /**
     * 传感器报表-湿度，参数分别是moId、beginTime、endTime、标记
     */
    STATS_HUM("HUM_STATIS:%s-%d-%d%s", 1),

    /**
     * 传感器报表-工时，参数为username
     */
    STATS_WORK("workhourReportFormData-%s", 1),

    /**
     * 传感器报表-正反转，参数分别是moId、beginTime、endTime、标记
     */
    STATS_VEER("VEER_STATIS:%s-%d-%d%s", 1),

    /**
     * 传感器报表-正反转-搜索，参数分别是moId、beginTime、endTime
     */
    STATS_VEER_SEARCH("VEER_STATIS:SEARCH_TIME:%s-%d-%d", 1),

    /**
     * 传感器报表-油量，参数分别是moId、beginTime、endTime
     */
    STATS_OIL_VOLUME("oilqs-%s-%d-%d", 1),

    /**
     * 传感器报表-油量-加漏油，参数分别是username、标记
     */
    STATS_OIL_VOLUME_LIST("OIL_IDS:%s%s", 1),

    /**
     * 传感器报表-油量数据，参数是id
     */
    STATS_OIL_DATA("OIL:DATA:%s", 1),

    /**
     * 传感器报表-里程，参数是username
     */
    STATS_MILEAGE("MILEAGE_IDS:%s", 1),

    /**
     * 传感器报表-里程，参数分别是moId、beginTime、endTime
     */
    STATS_MILEAGE_LIST("mileages-%s-%d-%d", 1),

    /**
     * 传感器报表-里程数据，参数是id
     */
    STATS_MILEAGE_DATA("MILEAGE:DATA:%s", 1),

    /**
     * 传感器报表-移动源基准能耗管理？，参数是username
     */
    STATS_MOBILE("MOB_IDS:%s", 1),

    /**
     * 传感器报表-移动源基准能耗管理对象？，参数是id
     */
    STATS_MOBILE_OBJECT("MOBOBJECT:%s", 1),

    /**
     * 传感器报表-油耗，参数分别是moId、beginTime、endTime
     */
    STATS_FUEL("fuelqs-%s-%d-%d", 1),

    /**
     * 传感器报表-油耗-用户，参数是username
     */
    STATS_FUEL_USER("%s_fuelConsumptionReport", 1),

    /**
     * 传感器报表-胎压，参数是username
     */
    STATS_TIRE("%s-tyrePressureReport-list", 1),

    /**
     * 传感器报表-油量里程，参数是username
     */
    STATS_OIL_MASS_MILE("%s-oilMassMileReport", 1),

    /**
     * 传感器报表-F3高精度，参数分别是moId、beginTime、endTime
     */
    STATS_F3("f3hpr-%s-%d-%d", 1),

    /**
     * 车辆报警信息
     */
    TIME_MONITOR_ALARM("%s:%s:%s", 14),

    /**
     * 车辆下发短信处理下发短信缓存
     */
    TIME_MONITOR_DEAL_MSG("%s:%s", 11),

    /**
     * 风控人员定时任务执行完成标志
     */
    CUSTOMER_SERVICE_JOB("%s_customer_service_job", 4),

    /**
     * 司机评分模块需要的企业id关系定时任务
     */
    DRIVER_SCORE_GROUP_IDS_JOB("%s_driver_score_group_ids_job", 4),

    /**
     * 主动安全参数设置， 车id 和风险 类型id
     */
    ADAS_VEHICLE_ALARM("%s_%s", 10),

    /**
     * 主动安全参数设置
     */
    ADAS_PARAM_VEHICLE_RISK_ID("RISK_%s_%s", 15),

    /**
     * 主动安全默认参数配置
     */
    DEFAULT_RISK_EVENT_SETTING_STR("DEFAULT_RISK_EVENT_SETTING_STR", 15),

    /**
     * 主动安全参数设置模糊删除
     */
    ADAS_PARAM_VEHICLE_RISK_ID_FUZZY("RISK_%s", 15),

    /**
     * 车辆状态报表前端打印需要获取疲劳和超速车牌号的key
     */
    VEH_STATE_REPORT_BRAND_KEY("vehicle_state_report_brand:%s", 11),
    /**
     * 中国adcode
     */
    CHINA_ADDRESS("china_address", 3),

    /**
     * 短信token认证用
     */
    USERNAME_CODE_COUNT("%s_code_count", 11),

    /**
     * 短信token认证用
     */
    USERNAME_CODE("%s_code", 11),

    /**
     * 普货监管报表--值班交接班记录
     */
    CARGO_WORK_HAND_OVER_RECORD_INFORMATION("%s_workHandOverRecord", 15),

    /**
     * 普货监管报表--离线车辆处置记录
     */
    CARGO_OFFLINE_REPORT_INFORMATION("%s_exportCargoOffLineReportInformation_list", 15),

    /**
     * 普货监管报表--普货抽查表
     */
    CARGO_SPOT_CHECK_INFORMATION("%s_cargo_spotCheck", 10),

    /**
     * 企业 - 货运车辆ID 缓存KEY
     */
    ORG_CARGO_VEHICLE("group_cargo_vehicle_%s", 10),

    /**
     * 企业 - 货运车辆ID 缓存KEY
     */
    ORG_CARGO_VEHICLE_PATTERN("group_cargo_vehicle_*", 10),

    /**
     * 两客一危--离线车辆报表
     */
    OFFLINE_REPORT_INFORMATION("%s_exportOffLineReportInformation_list", 2),

    /**
     * 视频参数设置HASH--存入redis的缓存 key是生成的uuid
     */
    VIDEO_SEND_PARAM("%s", 5),

    /**
     * 终端型号 参数：终端型号ID 类型：String
     */
    TERMINAL_TYPE_INFO("%s_TerminalTypeInfo", 9),

    /**
     * 海量点
     */
    MASSIVE_LOCATION("massive_location_points", 7),

    /**
     * 里程报表，参数：userId
     */
    SCHEDULED_MILEAGE_REPORT("%s_ScheduledMileageReport_list", 2),

    /**
     * 个呼号码，单点
     */
    PERSON_CALL_NUMBER("person_call_number", 12),

    /**
     * 个呼号码，群组
     */
    GROUP_CALL_NUMBER("group_call_number", 12),

    /**
     * 对讲
     */
    INTERCOM_LIST("%s_intercom_list", 9),

    /**
     * 对讲排序
     */
    INTERCOM_LIST_SORT("sort_intercom_info_list", 9),

    /**
     * 对讲搜索
     */
    INTERCOM_LIST_FUZZY("monitor_intercomDeviceId_simcard_fuzzy_search", 9),

    /**
     * 实时监控日志
     */
    EXPORT_LOG_FIND_INFORMATION("%s_exportLogFindInformation_list", 2),

    /**
     * 音视频日志
     */
    EXPORT_VIDEO_LOG_INFORMATION("%s_exportVideoLogInformation_list", 2),

    /**
     * 出勤报表，参数为userId
     */
    SCHEDULED_REPORT("%s_ScheduledReport_list", 2),

    /**
     * app意见反馈，参数为userId
     */
    APP_FEED_BACK("%s_exportAppFeedBack_list", 2),

    /**
     * 车牌token
     */
    VEHICLE_BRAND_TOKEN("vehicle_brand%s", 4),
    /**
     * 单车登录信息，参数为token，值为vehicleId
     */
    SINGLE_VEHICLE_TOKEN("vehicle_brand%s", 4),

    /**
     * 单车报警信息，参数为vehicleId
     */
    SINGLE_VEHICLE_ALARM("%s_singleAlarmDeal_list", 11),
    /**
     * 性能监控存放的ip
     */
    IP_ADDRESS("%s", 11),

    /**
     * 开关信号报表表格数据 KEY 终端
     */
    SWITCH_SIGNAL_REPORT_FORM_DATA_TERMINAL_KEY("switchSignalReportFormData_terminal-%s", 1),

    /**
     * io采集1
     */
    SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_ONE_KEY("switchSignalReportFormData_acquisitionBoardOne-%s", 1),

    /**
     * io采集2
     */
    SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_TWO_KEY("switchSignalReportFormData_acquisitionBoardTwo-%s", 1),

    /**
     * 定制列
     */
    CUSTOM_COLUMN("%s", 2),

    /**
     * 定时保存需要计算离线报表的排班相关的key
     */
    SCHEDULED("scheduled_%s", 1),

    /**
     * 载重传感器，参数分别是username、载重序号（0或1)
     */
    LOAD_SENSOR_INFO("%s_loadChartInfo_%d_list", 10),

    /**
     * 载重传感器状态，参数分别是载重序号（0或1)、username、载重状态
     */
    LOAD_SENSOR_STATUS("loadStatus_%d_%s_%d", 10),

    /**
     * 载重传感器状态模糊查询，参数分别是载重序号（0或1)、username
     */
    LOAD_SENSOR_STATUS_PATTERN("loadStatus_%d_%s*", 10),

    /**
     * 闪烁的报警类型，参数是userId
     */
    ALARM_SETTING_FLICKER("%s_alarmSetting_flicker", 0),

    /**
     * 发声的报警类型，参数是userId
     */
    ALARM_SETTING_SOUND("%s_alarmSetting_sound", 0),
    /**
     * 导出暂时存在缓存中的数据
     */
    TMP_EXPORT_DATA("%s_%s_list", 2),

    /**
     * SwitchSignalStatisticsServiceImpl 用的key
     */
    SWITCH_SIGNAL_STATISTIC_KEY("%s", 1),

    /**d
     * 每个企业下所有child的uuid给paas api
     */
    ORG_CHILD_UUID_LIST("%s_org_child_uuid_list", 9),
    ;

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public int getDbIndex() {
        return dbIndex;
    }

    private final String pattern;
    private final int dbIndex;

    /**
     * 根据标识获取 （自定义列使用）
     */
    public static RedisKey getCustomColumnEnum(String key) {
        for (HistoryRedisKeyEnum e : HistoryRedisKeyEnum.values()) {
            if (e.pattern.equals(key)) {
                return e.of();
            }
        }
        return null;
    }

    /**
     * 获取用户的自定义列key
     */
    public static HistoryRedisKeyEnum getUserCustomColumnEnum(String key) {
        for (HistoryRedisKeyEnum e : HistoryRedisKeyEnum.values()) {
            if (("%s_" + key).equals(e.pattern)) {
                return e;
            }
        }
        return null;
    }
}

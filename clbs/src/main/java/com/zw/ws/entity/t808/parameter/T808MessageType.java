/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.parameter;

/**
 * <p>
 * Title: T808MessageType.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年8月10日下午6:15:18
 */
public class T808MessageType {
    public static final int DEVICE_LOCATION_INFO_UPLOAD = 0x0200;// 设备位置信息上报

    public static final int DEVICE_SUBSCRIPTION = 0x0000;// 单条订阅消息

    public static final int PLANT_START = 0x0ABB;// 第三方平台连接

    public static final int PLANT_CLOSE = 0x1ABB;// 第三方平台关闭
    public static final int PLANT_SUBSCRIPTION_LIST = 0x00BB;// 第三方平台订阅

    public static final int PLANT_CAN_SUBSCRIPTION_LIST = 0x11BB;// 取消第三方订阅

    public static final int DEVICE_SUBSCRIPTION_LIST = 0x00AA;// 多条订阅消息

    public static final int DEVICE_CAN_SUBSCRIPTION_LIST = 0x11AA;// 多条订阅消息

    public static final int DEVICE_CAN_SUBSCRIPTION = 0x1111;// 单条取消订阅消息

    public static final int SUBSCRIPTION_LIST = 0x0B01;// 订阅列表

    public static final Integer QUERY_DEVICE_PARAMETER = 0x8104;// 查询终端参数

    public static final int CLIENT_SEND_QUERY_DEVICE_PARAMETER_COMMAND = 0x8104;// 客户端发送查询终端参数指令

    public static final int CLIENT_SEND_TAKE_PHOTO_COMMAND = 0x8801;// 客户端发送拍照指令_（包含快速拍照）

    public static final int CLIENT_SEND_QUERY_LOCATION_COMMAND = 0X8201;// 客户端发送点名指令（位置信息查询）

    public static final int CLIENT_SEND_QUERY_LOCATION_PARAMETERS = 0X8202;//临时位置跟踪控制

    public static final int CLIENT_SEND_QUERY_LOCATION_RESPONSE = 0x0201; // 位置信息查询应答

    public static final int CLIENT_SEND_TEXT_MESSAGE_COMMAND = 0x8300;// 客户端下发文本信息

    public static final int CLIENT_REQUEST_SETTING_DEVICE_PARAMETER = 0x8103;// 客户端请求设置终端参数

    public static final int CLIENT_QUERY_CLIENT_SPECIFY_PARAMETER = 0x8106;// 查询终端指定参数

    public static final int DEVICE_PROPERTY_QUERY = 0x8107;// 查询终端属性

    public static final int SEND_ELECTRONIC_LINE_DEFENCE_COMMAND = 0x8606;// 下发路线电子围栏

    public static final int SEND_ELECTRONIC_CIRCLE_AREA_DEFENCE_COMMAND = 0X8600;// 下发圆形区域围栏

    public static final int SEND_ELECTRONIC_RECTANGLE_AREA_DEFENCE_COMMAND = 0X8602;// 下发矩形区域围栏

    public static final int SEND_ELECTRONIC_POLYGON_AREA_DEFENCE_COMMAND = 0X8604;// 下发多边形区域围栏

    public static final int DELETE_ELECTRONIC_CIRCLE_ZONE_COMMAND = 0x8601;// 删除圆形区域

    public static final int DELETE_ELECTRONIC_RECTANGLE_ZONE_COMMAND = 0x8603;// 删除矩形区域

    public static final int DELETE_ELECTRONIC_LINE_COMMAND = 0x8607;// 删除线路

    public static final int DELETE_ELECTRONIC_POLYGON_COMMAND = 0x8605;// 删除多边形

    public static final int DEVICE_REGISTER = 0x0100;// 终端注册

    public static final int DEVICE_REGISTER_ACK = 0x8100;// 终端注册应答

    public static final int SEND_DELETE_DEFENCE_COMMAND = 0x8607;// 删除电子围栏

    public static final int DEVICE_COMMON_RESPONSE = 0x0001;// 设备（终端）通用应答

    public static final int RECIEVED_DEVICE_HEARTBEAT = 0x0002;// 终端心跳

    public static final int DEVICE_REGISTER_RESPONSE = 0x8100;//终端注册应答

    public static final int DEVICE_UPLOAD_ACK = 0x0108;//设备升级结果

    public static final int DEVICE_PHOTOGRAPH = 0x8801;//拍照

    public static final int DEVICE_PROPERTY_ACK = 0x0107;//拍照


    public static final int DEVICE_SEND_TXT= 0x8300;//文本信息下发

    public static final int DEVICE_SEND_QUESTION= 0x8302;//文本信息下发

    public static final int DEVICE_ALARM_ACK= 0x8203;//人工确认报警

    public static final int DEVICE_COMMAND= 0x8105;//终端控制

    public static final int VEHIVLE_COMMAND= 0x8500;//车辆控制

    public static final int DATA_THROUG_DOWNLOAD_COMMAND = 0x8900;//透传下行消息

    public static final int DEVICE_OFFLINE = 0x9999;//设备离线

    public static final int DEVICE_MULTIMEDIA_UP = 0x0801;//多媒体信息上传

    public static final int DEVICE_MULTIMEDIA_RE_UP = 0x1801;//多媒体信息补传命令

    public static final int DEVICE_MULTIMEDIA_RE_INFORM = 0x2801;//多媒体信息接收完成通知

    public static final String DEVICE_KKS_YX_LISTKAY = "devicekksyxlist";//康卡斯有线

    public static final String DEVICE_KKS_WX_LISTKAY = "devicekkswxlist";//康凯斯无线


    public static final String DEVICE_ASO_CCDJ_LISTKAY = "devicekksyxlist";//艾赛欧超长待机

    public static final String DEVICE_F3_CCDJ_LISTKAY = "devicekkswxlist";//F3超长待机设备

    public static final String DEVICE_BSJ_LISTKAY = "devicekkswxlist";//博实结

    public static final String DEVICE_808_LISTKAY = "device808list";

    public static final String DEVICE_808_11_LIST_KAY = "device808list_11";

    public static final String DEVICE_GV320_LISTKAY = "device320list";

    public static final String DEVICE_BD_LISTKAY = "devicebdlist";

    public static final String VEHICLE_PR = "vehicle";//车辆前缀标识
    public static final String DEVICE_TH_LISTKAY = "TH";

    public static final int AUTHRNTICATION_INFO=0x0102;//鉴权信息

    public static final int DEVICE_RECORD = 0x8804;// 客户端下发录音指令

    public static final int TEL_BACK = 0x8400;// 电话汇报
    public static final int VEHICLE_STATUS = 0x2222;// 车辆启用状态
    public static final int DEVICE_STATUS = 0x3333;// 终端启用状态
    public static final int ELECTORNIC_WAYBILL_REPORT = 0x0701; // 电子运单上报
    public static final int DRIVER_INFO_COLLECTION_REPORT =  0x0702; // 驾驶员身份信息采集上报

    public static final int INFORMATION_DEMAND_OR_CANCEL =  0x0303; // 信息点播/取消

    public static final int QUESTION_RESPONSE_REPORT = 0x0302; // 提问应答上报

    public static final int DATA_PERMEANCE_REPORT = 0x0900; // 数据透传上报

    public static final int RECORD_COLLECTION = 0x8700;// 记录采集
    public static final int MULTIMEDIA_RETRIEVAL = 0x8802;// 多媒体检索
    public static final int MULTIMEDIA_UPLOAD = 0x8803;// 多媒体上传
    public static final int SET_EVENT = 0x8301;// 事件设置
    public static final int SET_INFO_DEMAND_MENU = 0x8303;// 信息点播菜单设置
    public static final int SET_PHONE_BOOK = 0x8401;// 设置电话本
    public static final int DEVICE_PARAM_ACK = 0x0104;// 终端参数应答
    public static final int EVENT_REPORT = 0x0301;// 事件报告

    public static final int UNLOCK_LOCK = 0x0500;//解锁加锁

    public static final int INFORMATION_SERVICE = 0x8304;// 信息服务

    public static final int BDTD_LOCATION = 0xB3;// 手环位置信息

    public static final int BDTD_ALARM = 0xB4;
}

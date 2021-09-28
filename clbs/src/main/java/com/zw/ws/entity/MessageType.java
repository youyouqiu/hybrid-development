package com.zw.ws.entity;

public class MessageType {
    public static final int ClIENT_REQUEST_VEHICLE_INFO = 0xB003;// 客户端请求车辆信息

    public static final int ClIENT_REQUEST_VEHICLE_INFO_RESPONSE = 0x2003;// 客户端请求车辆信息应答

    public static final int ClIENT_REQUEST_DRIVER_INFO = 0xB004;// 客户端请求司机信息

    public static final int ClIENT_REQUEST_DRIVER_INFO_RESPONSE = 0x2004;// 客户端请求司机信息应答

    public static final int CLIENT_REQUEST_ORGANIZATION_INFO = 0xB002;// 客户端请求组织架构信息

    public static final int CLIENT_REQUEST_ORGANIZATION_INFO_RESPONSE = 0x2002;// 客户端请求组织架构信息应答

    public static final int ClIENT_REQUEST_SAVING_POINT = 0xB101;// 客户端请求保存点数据

    public static final int ClIENT_REQUEST_SAVING_POINT_RESPONSE = 0x2101;// 客户端请求保存点数据应答

    public static final int CLIENT_REQUEST_SAVING_LINE = 0xB102;// 客户端请求保存线路数据

    public static final int CLIENT_REQUEST_SAVING_LINE_RESPONSE = 0x2102;

    public static final int CLIENT_REQUEST_SAVING_RECTANGLE = 0xB103;// 客户端请求保存矩形数据

    public static final int CLIENT_REQUEST_SAVING_RECTANGLE_RESPONSE = 0x2103;

    public static final int CLIENT_REQUEST_SAVING_POLYGON = 0xB104;// 客户端请求保存多边形数据

    public static final int CLIENT_REQUEST_SAVING_POLYGON_RESPONSE = 0x2104;

    public static final int CLIENT_REQUEST_SAVING_CIRCLE = 0xB105;// 客户端请求保存圆形数据

    public static final int CLIENT_REQUEST_SAVING_CIRCLE_RESPONSE = 0x2105;

    public static final int CLIENT_REQUEST_SUBSCRIBE_VEHICLE_INSTANCEINFO = 0xA004;// 客户端请求订阅车辆实时数据

    public static final int WEBSOCKET_PUSH_VEHICLE_STATUS_INFO = 0x1007;// WebSocket推送状态信息

    public static final int WEBSOCKET_PUSH_VEHICLE_ALARM_INFO = 0x1009;// WebSocket推送报警信息

    public static final int CLIENT_REQUEST_DEFENCE = 0xB106; // 客户端请求电子围栏信息

    public static final int CLIENT_REQUEST_DEFENCE_POINT_RESPONSE = 0x2106; // 客户端请求点数据应答

    public static final int CLIENT_REQUEST_DEFENCE_LINE_RESPONSE = 0x2107; // 客户端请求线路数据应答

    public static final int CLIENT_REQUEST_DEFENCE_RCTANGLE_RESPONSE = 0x2108; // 客户端请求矩形数据应答

    public static final int CLIENT_REQUEST_DEFENCE_POLYGON_RESPONSE = 0x2109; // 客户端请求多边形数据应答

    public static final int CLIENT_REQUEST_DEFENCE_CIRCLE_RESPONSE = 0x210A; // 客户端请求圆形数据应答

    public static final int CLIENT_REQUEST_HISTORY_TRACK_INFO = 0xA303;//客户端请求历史轨迹数据

    public static final int CLIENT_REQUEST_HISTORY_TRACK_INFO_RESPONSE = 0x1303;//客户端请求历史轨迹数据应答

    public static final int CLIENT_REQUEST_VEHICLE_ACTIVE_DAYS = 0xA302;//客户端请求车辆当月活跃天数

    public static final int CLIENT_REQUEST_VEHICLE_ACTIVE_DAYS_RESPONSE = 0x1302;//客户端请求当月车辆活跃天数应答

    public static final int CLIENT_REQUEST_VEHICLE_OIL_USING = 0xA309;//客户端请求油耗数据

    public static final int CLIENT_REQUEST_VEHICLE_OIL_USING_RESPONSE = 0x1309;//客户端请求油耗数据应答

    public static final int CLIENT_REQUEST_VEHICLE_HISTORY_ALARM_INFO = 0xA307;//客户端请求历史报警信息

    public static final int CLIENT_REQUEST_VEHICLE_HISTORY_ALARM_INFO_RESPONSE = 0x1307;//客户端请求历史报警信息应答

    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_STATUS_INTO = 0x9999;//BS端请求车辆缓存状态信息
    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_ADD_INTO = 0x8888;//BS端请求车辆增加缓存状态信息
    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO = 0x7777;//BS端请求车辆更新缓存状态信息

}

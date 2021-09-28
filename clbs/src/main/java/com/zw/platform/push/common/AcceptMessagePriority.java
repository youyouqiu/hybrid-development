package com.zw.platform.push.common;

import com.zw.platform.util.ConstantUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author penghj
 * @version 1.0
 */
public class AcceptMessagePriority {

    /**
     * msgType -> msgId -> priority
     */
    private static final Map<Integer, Map<Integer, Integer>> PRIORITY_MAP = new HashMap<>(4);

    static {
        Map<Integer, Integer> t808Map = new LinkedHashMap<>(64);
        Map<Integer, Integer> t809Map = new LinkedHashMap<>(64);
        PRIORITY_MAP.put(ConstantUtil.MSG_TYPE_GENERAL_PROTOCOL, t808Map);
        PRIORITY_MAP.put(ConstantUtil.MSG_TYPE_T809_PROTOCOL, t809Map);
        int i = 0;
        // 终端注册0x0100
        t808Map.put(ConstantUtil.T808_REGISTER, i++);
        // 设备离线 0x0D09
        t808Map.put(ConstantUtil.WEB_DEVICE_OFF_LINE, i++);
        // 新增车辆状态信息 0x8888
        t808Map.put(ConstantUtil.BS_CLIENT_REQUEST_VEHICLE_CACHE_ADD_INTO, i++);
        // 更新车辆状态信息 0x7777
        t808Map.put(ConstantUtil.BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO, i++);

        // 文件上传完成通知 0x1208 事件推送
        t808Map.put(ConstantUtil.T808_RSP_MEDIA_STORAGE_FTP_1208, i++);
        // 视频资源列表应答 0x1205 websocket推送
        t808Map.put(ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK, i++);
        // 视频资源日期应答 0x120F websocket推送
        t808Map.put(ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK_BEFOR, i++);
        // 终端属性查询应答 0x0107 websocket推送
        t808Map.put(ConstantUtil.T808_ATTR_ACK, i++);

        // 终端上传乘客流量 0x1005 记日志 websocket推送
        t808Map.put(ConstantUtil.DEVICE_UPLOAD_RIDERSHIP, i++);
        // 终端上传音视频属性 0x1003 记日志 websocket推送
        t808Map.put(ConstantUtil.DEVICE_UPLOAD_VIDEO_PARAM, i++);
        // 存储多媒体数据检索应答 0x0802 记日志 websocket推送
        t808Map.put(ConstantUtil.T808_MULTIMEDIA_SEARCH_ACK, i++);
        // 保存提问应答上报日志 0x0302 记日志 websocket推送
        t808Map.put(ConstantUtil.T808_ANSWER, i++);
        // 事件报告存储日志 0x0301 记日志 websocket推送
        t808Map.put(ConstantUtil.T808_E_REPORT, i++);
        // 保存电子运单上报日志 0x0701 记日志 websocket推送
        t808Map.put(ConstantUtil.T808_E_AWB, i++);

        // 人脸识别请求 0X9242 下发0x1412
        t809Map.put(ConstantUtil.T809_UP_EXG_MSG_FACE_PHOTO_REQ, i++);

        // 静态信息交换消息 0x9600 下发0x1601/0x1602/0x1607
        t809Map.put(ConstantUtil.T809_DOWN_BASE_MSG, i++);

        // 通用应答 0x0001 websocket推送 更新数据 入库 取消超时应答
        t808Map.put(ConstantUtil.T808_DEVICE_GE_ACK, i++);
        // 终端驾驶员身份库查询应答 0x0E22 websocket推送 更新数据 入库 取消超时应答
        t808Map.put(ConstantUtil.QUERY_DEVICE_DRIVER_REQ_HUNAN_ACK, i++);
        // 终端驾驶员身份库查询应答 0x0E12 websocket推送 更新数据 入库 取消超时应答
        t808Map.put(ConstantUtil.QUERY_DEVICE_DRIVER_REQ_ACK, i++);

        // 驾驶员人证照片更新请求 0x1507 下发0x9507
        t808Map.put(ConstantUtil.T808_REQ_MEDIA_UPDATE, i++);
        // 驾驶员身份识别上报 0x0E10 入库
        t808Map.put(ConstantUtil.DRIVER_IDENTIFICATION_REPORT, i++);

        // 查询终端参数应答 0x0104 记日志 websocket推送 更新数据
        t808Map.put(ConstantUtil.T808_PARAM_ACK, i++);
        // 外设升级结果消息 0x0108 记日志 websocket推送 更新数据
        t808Map.put(ConstantUtil.T808_UPLOAD_ACK, i++);
        // 数据透传上报 0x0900 记日志 websocket推送 更新数据 入库
        t808Map.put(ConstantUtil.T808_DATA_PERMEANCE_REPORT, i++);
        // 行驶记录仪数据上传 0x0700 记日志 websocket推送 更新数据
        t808Map.put(ConstantUtil.T808_DRIVER_RECORD_UPLOAD, i++);

        // 视频多媒体文件上传完成通知 0x1206 记日志 websocket推送 入库 文件操作
        t808Map.put(ConstantUtil.ADAS_UP_EXG_MSG_RETURN_END_ACK, i++);
        // 多媒体数据处理 0x0801 记日志 websocket推送 更新数据 入库 文件操作
        t808Map.put(ConstantUtil.T808_MULTIMEDIA_DATA, i++);
        // 驾驶员身份信息采集上报 0x0702 记日志 websocket推送 更新数据 入库 文件操作
        t808Map.put(ConstantUtil.T808_DRIVER_INFO, i++);
        // 保存驾驶员识别信息收集上报日志 0x0706 websocket推送 更新数据 入库 下发0x9208
        t808Map.put(ConstantUtil.T808_DRIVER_IDENTIFY, i++);
        // 处理平台巡检的应答结果 0x0710 websocket推送 更新数据 入库 下发0x9208
        t808Map.put(ConstantUtil.T808_FLATFORM_INSPECTION_ACK, i++);
        // 保存信息点播/取消上报日志 0x0303 记日志 websocket推送 下发0x8304
        t808Map.put(ConstantUtil.T808_INFO_MANAGER, i++);

        t809Map.put(ConstantUtil.T809_UP_CONNECT_REQ, i++);
        t809Map.put(ConstantUtil.T809_UP_CONNECT_RSP, i++);
        t809Map.put(ConstantUtil.T809_UP_DISCONNECT_REQ, i++);
        t809Map.put(ConstantUtil.T809_UP_DISCONNECT_RSP, i++);
        t809Map.put(ConstantUtil.T809_UP_LINKTEST_RSP, i++);
        t809Map.put(ConstantUtil.T809_UP_DISCONNECT_INFORM, i++);
        t809Map.put(ConstantUtil.T809_UP_CLOSELINK_INFORM, i++);
        t809Map.put(ConstantUtil.T809_DOWN_CONNECT_REQ, i++);
        t809Map.put(ConstantUtil.T809_DOWN_CONNECT_RSP, i++);
        t809Map.put(ConstantUtil.T809_DOWN_DISCONNECT_REQ, i++);
        t809Map.put(ConstantUtil.T809_DOWN_DISCONNECT_RSP, i++);
        t809Map.put(ConstantUtil.T809_DOWN_LINKTEST_REQ, i++);
        t809Map.put(ConstantUtil.T809_DOWN_LINKTEST_RSP, i++);
        t809Map.put(ConstantUtil.T809_DOWN_DISCONNECT_INFORM, i++);
        t809Map.put(ConstantUtil.T809_DOWN_CLOSELINK_INFORM, i++);
        t809Map.put(ConstantUtil.T809_DOWN_WARN_MSG, i++);
        t809Map.put(ConstantUtil.T809_DOWN_ENTERPRISE_ON_DUTY_REQ, i++);
        t809Map.put(ConstantUtil.T809_DOWN_ENTERPRISE_HANDLE_REQ, i++);
        t809Map.put(ConstantUtil.WEB_809_CHECK_SERVER_STATUS_RSP, i++);
        t809Map.put(ConstantUtil.T809_DOWN_PREVENTION_MSG, i++);
        t809Map.put(ConstantUtil.T809_DOWN_TOTAL_RECV_BACK_MSG, i++);
        t809Map.put(ConstantUtil.T809_DOWN_EXG_MSG, i++);

        t809Map.put(ConstantUtil.T809_DOWN_PLATFORM_MSG_INFO_REQ, i++);
        t809Map.put(ConstantUtil.T809_DOWN_PLATFORM_MSG, i++);
        t809Map.put(ConstantUtil.T809_DOWN_PLATFORM_MSG_POST_QUERY_REQ, i++);


        // 围栏查询应答 0x0608 记日志
        t808Map.put(ConstantUtil.T808_FENCE_QUERY_RESP, i++);

        // 位置信息查询应答 0x0201
        t808Map.put(ConstantUtil.T808_GPS_INFO_ACK, i++);
        // 位置信息 0x0200 0x0500
        t808Map.put(ConstantUtil.T808_GPS_INFO, i++);
        t808Map.put(ConstantUtil.T808_VEHICLE_CONTROL_ACK, i++);
        // 手环位置信息 0xB3
        t808Map.put(ConstantUtil.BDTD_LOCATION, i++);
        // 位置数据补传 0x0704
        t808Map.put(ConstantUtil.T808_BATCH_GPS_INFO, i++);
        // 终端报警(现在的报警全部由flink通过zmq推送不走netty) 0x0C10
        t808Map.put(ConstantUtil.WEB_ALARM_DATA, i++);


    }

    public static Integer getMessagePriority(Integer msgType, Integer msgId) {
        Map<Integer, Integer> map = PRIORITY_MAP.get(msgType);
        if (map == null || map.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return map.getOrDefault(msgId, Integer.MAX_VALUE);
    }
}

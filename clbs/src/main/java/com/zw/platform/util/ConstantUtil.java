package com.zw.platform.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.platform.domain.enmu.ProtocolEnum;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by LiaoYuecai on 2017/6/19. 常量配置/引用
 */
public class ConstantUtil {

    /**
     * 消息类型 1：普通协议 2：809协议
     */
    public static final Integer MSG_TYPE_GENERAL_PROTOCOL = 1;
    /**
     * 消息类型 1：普通协议 2：809协议
     */
    public static final Integer MSG_TYPE_T809_PROTOCOL = 2;

    // 运算常量
    public static final Integer BYTE = 0xFF;// byte运算补符号位位

    public static final Integer SHORT = 0xFFFF;// short运算补符号位位

    public static final Long INT = 0xFFFFFFFFL;// int运算补符号位位

    public static final String ADAS_EVENT_CODE_0x9206 = "ADAS_0x9206";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMddHHmmss");// 时间日期统一格式

    // T808标识符
    public static final Byte T808_ESCAPE_TAG = 0x7D;// 转义字符

    public static final Byte T808_ESCAPE_FILLING_BYTE_7E = 0x02;

    public static final Byte T808_ESCAPE_FILLING_BYTE_7D = 0x01;

    public static final Byte T808_MEG_TAG = 0x7E;// 消息 开始/结束 标识符

    public static final int VIDEO_ID = 0x0C01;// 视频消息下发标识

    public static final String CLIENT_MOBILE = "00000000000";// 客户端模拟协议填充的手机号
    // T808消息体属性运算符

    public static final int T808_MSG_BODY_SIZE_BIT = 0x03FF;

    public static final int T808_MSG_SUB_FLAG_BIT = 0x2000;

    public static final int T808_MSG_ENCODE_MOD_BIT = 0x1C00;

    // T808消息ID标识
    public static final int T808_REISSUE_SUBPACKAGE = 0x8003;

    public static final int T808_PLANT_GE_ACK = 0x8001;

    public static final int T808_REGISTER_ACK = 0x8100;

    public static final int T808_SET_PARAM = 0x8103;

    public static final int T808_DEVICE_CONTROLLER = 0x8105;

    public static final int T808_QUERY_PARAMS = 0x8106;

    public static final int T808_DEVICE_UPLOAD = 0x8108;

    public static final int T808_QUERY_LOCATION_COMMAND = 0X8201;// 客户端发送点名指令（位置信息查询）

    public static final int T808_INTERIM_TRACE = 0x8202;

    public static final int T808_ALARM_ACK = 0x8203;

    public static final int T808_SEND_TXT = 0x8300;

    public static final int T808_SET_EVENT = 0x8301;

    public static final int T808_SEND_QUIZ = 0x8302;

    public static final int T808_SET_INFO_MENU = 0x8303;

    public static final int T808_INFO_MSG = 0x8304;

    public static final int T808_CALL_BACK = 0x8400;

    public static final int T808_SET_PHONE_BOOK = 0x8401;

    public static final int T808_VEHICLE_CONTROLLER = 0x8500;

    public static final int T808_SET_CIRCULAR_AREA = 0x8600;

    public static final int T808_TRAVEL_RECORD_COLLECT = 0x8700;

    public static final int T808_TRAVEL_RECORD_DOWN = 0x8701;

    public static final int T808_SET_RECTANGLE_AREA = 0x8602;

    public static final int T808_SET_POLYGON_AREA = 0x8604;

    public static final int T808_SET_LINE = 0x8606;

    public static final int T808_DELETE_LINE = 0x8607;

    public static final int T808_DELETE_ROUND_AREA = 0x8601;

    public static final int T808_DELETE_RECTANGLE_AREA = 0x8603;

    public static final int T808_DELETE_POLYGON_AREA = 0x8605;

    public static final int T808_MULTIMEDIA_ACK = 0x8800;

    public static final int T808_PHOTOGRAPH = 0x8801;

    public static final int T808_MULTIMEDIA_SEARCH = 0x8802;

    public static final int T808_MULTIMEDIA_UPLOAD = 0x8803;

    public static final int T808_RECORD_COMMAND = 0x8804;

    public static final int T808_ONE_MULTIMEDIA_UP = 0x8805;

    public static final int T808_PENETRATE_DOWN = 0x8900;

    public static final int T808_PLANT_FORM_RSA_KEY = 0x8A00;

    public static final int T808_DEVICE_RSA_KEY = 0x0A00;

    public static final int T808_COMPRESS = 0x0901;

    public static final int T808_PENETRATE_UP = 0x0900;

    public static final int T808_CAMERA_ACK = 0x0805;

    public static final int T808_MULTIMEDIA_SEARCH_ACK = 0x0802;

    public static final int T808_MULTIMEDIA_DATA = 0x0801;

    public static final int T808_MULTIMEDIA_EVENT = 0x0800;

    public static final int T808_DRIVER_IDENTIFY = 0x0706;

    public static final int T808_DRIVER_IDENTIFY_INSPECTION = 0x9706;

    public static final int T808_FLATFORM_INSPECTION = 0x9710;

    public static final int T808_FLATFORM_INSPECTION_ACK = 0x0710;

    public static final int T808_CAN_DATA_UPLOAD = 0x0705;

    public static final int T808_BATCH_GPS_INFO = 0x0704;

    public static final int T808_DRIVER_INFO = 0x0702;

    public static final int T808_E_AWB = 0x0701;

    public static final int T808_DRIVER_RECORD_UPLOAD = 0x0700;

    public static final int T808_VEHICLE_CONTROL_ACK = 0x0500;

    public static final int T808_INFO_MANAGER = 0x0303;

    public static final int T808_ANSWER = 0x0302;

    public static final int T808_E_REPORT = 0x0301;

    public static final int T808_GPS_INFO_ACK = 0x0201;

    public static final int T808_GPS_INFO = 0x0200;

    public static final int T808_UPLOAD_ACK = 0x0108;

    public static final int T808_ATTR_ACK = 0x0107;

    public static final int T808_PARAM_ACK = 0x0104;

    public static final int T808_AUTH = 0x0102;

    public static final int T808_REGISTER = 0x0100;

    /**
     * 链路检测
     */
    public static final int T808_LINK_CHECK = 0x8204;

    /**
     * 围栏查询下发
     */
    public static final int T808_FENCE_QUERY = 0x8608;

    /**
     * 围栏查询应答
     */
    public static final int T808_FENCE_QUERY_RESP = 0x0608;

    public static final int T808_DEVICE_GE_ACK = 0x0001;// 通用应答

    public static final int T808_DATA_PERMEANCE_REPORT = 0x0900; // 数据透传上报

    public static final int T808_DEVICE_PROPERTY_QUERY = 0x8107;// 查询终端属性

    public static final int T808_DEVICE_PARAMETER_COMMAND = 0x8104;// 客户端发送查询终端参数指令

    public static final Integer T808_MAX_PAG_LEN = 1024;// 最大包长度

    public static final Charset T808_STRING_CODE = Charset.forName("GBK");// GBK编码

    public static final int T808_VIDEO_TRANSMIT_CONTROL = 0x9102; // 音视频实时传输控制指令下发

    public static final int T808_ACCESS_PLATFORM_OPEN = 0x20AA; // 增加T808转入平台认证

    public static final int T808_ACCESS_PLATFORM_CLOSE = 0x20BB; // 删除T808转入平台认证

    // web标识
    public static final int WEB_SUBSCRIBE_STATUS = 0x0C00;

    public static final int WEB_CAN_SUBSCRIBE_STATUS = 0x0D00;

    public static final int WEB_SUBSCRIBE_POSITION = 0x0C02;

    public static final int WEB_CAN_SUBSCRIBE_POSITION = 0x0D02;

    public static final int WEB_DEVICE_OFF_LINE = 0x0D09;

    public static final int WEB_DEVICE_ON_LINE = 0x0C09;

    public static final int WEB_809_CHECK_START = 0x0C03;

    public static final int WEB_809_CHECK_END = 0x0D03;

    /**
     * 平台是否过滤无效数据
     */
    public static final int WEB_809_INVALID_FILTER = 0x0F01;

    public static final int WEB_808_TRANSMIT_ADD = 0x0C04;// 808转发目标增加

    public static final int WEB_808_TRANSMIT_DEL = 0x0D04;// 808转发目标减少

    public static final int WEB_808_TRANSMIT_DEVICE_ADD = 0x0C05;// 808转发绑定设备增加

    public static final int WEB_808_TRANSMIT_DEVICE_DEL = 0x0D05;// 808转发绑定设备减少

    public static final int WEB_809_CHECK_SERVER_STATUS_REQ = 0x0C06;// 请求过检服务器状态

    public static final int WEB_809_CHECK_SERVER_STATUS_RSP = 0x0D06;// 应答过检服务器状态

    public static final int WEB_INFORM_CLIENT_ID = 0x0C07;// 通知当前连接唯一标识

    public static final int WEB_DEVICE_UNBOUND = 0x0DFF;// 解除绑定

    public static final int WEB_BATCH_DEVICE_UNBOUND = 0x0FFF;// 批量解除绑定

    /**
     * 慎用:
     * 删除F3非法注册设备缓存, 这会导致f3会从数据库中查询数据, 现在仅信息配置导入使用.
     * 验证是否成功流程: 先发注册，再导入, 如果能成功上线则不会存在问题.
     */
    public static final int DELETE_F3_INVALID_CACHE = 0x0FFE;

    public static final int WEB_DEVICE_BOUND = 0x0CFF;// 绑定

    public static final int WEB_BATCH_DEVICE_BOUND = 0xCCFF;// 信息配置批量绑定

    public static final int WEB_GLOBAL_OFF = 0x0BFF;// 解除全局报警
    // T809标识及转义

    public static final int T809_HEAD = 0x5b;

    public static final int T809_TAIL = 0x5d;

    public static final int T809_ESCAPE_A = 0x5a;

    public static final int T809_ESCAPE_E = 0x5e;

    public static final int T809_ESCAPE_1 = 0x01;

    public static final int T809_ESCAPE_2 = 0x02;

    public static final int T809_ESCAPE_A_1 = 0x5b;

    public static final int T809_ESCAPE_E_1 = 0x5d;

    public static final int T809_ESCAPE_A_2 = 0x5a;

    public static final int T809_ESCAPE_E_2 = 0x5e;

    public static final int PLANT_MIN_LEN = 26;// T809消息最小长度（消息体为空时消息长度）
    // T809业务数据类型名称标识

    public static final int T809_UP_CONNECT_REQ = 0x1001;

    public static final int T809_UP_CONNECT_RSP = 0x1002;

    public static final int T809_UP_DISCONNECT_REQ = 0x1003;

    public static final int T809_UP_DISCONNECT_RSP = 0x1004;

    public static final int T809_UP_LINKETEST_REQ = 0x1005;

    public static final int T809_UP_LINKTEST_RSP = 0x1006;

    public static final int T809_UP_DISCONNECT_INFORM = 0x1007;

    public static final int T809_UP_CLOSELINK_INFORM = 0x1008;

    public static final int T809_UP_AUTHORIZE_MSG = 0x1700;//主链路-时效口令上报

    public static final int T809_UP_AUTHORIZE_MSG_STARTUP = 0x1701;//主链路-时效口令上报

    public static final int T809_DOWN_CONNECT_REQ = 0x9001;

    public static final int T809_DOWN_CONNECT_RSP = 0x9002;

    public static final int T809_DOWN_DISCONNECT_REQ = 0x9003;

    public static final int T809_DOWN_DISCONNECT_RSP = 0x9004;

    public static final int T809_DOWN_LINKTEST_REQ = 0x9005;

    public static final int T809_DOWN_LINKTEST_RSP = 0x9006;

    public static final int T809_DOWN_DISCONNECT_INFORM = 0x9007;

    public static final int T809_DOWN_CLOSELINK_INFORM = 0x9008;

    public static final int T809_DOWN_TOTAL_RECV_BACK_MSG = 0x9101;

    public static final int T809_UP_EXG_MSG = 0x1200;

    public static final int T809_DOWN_EXG_MSG = 0x9200;

    public static final int T809_UP_PLATFORM_MSG = 0x1300;

    public static final int T809_DOWN_PLATFORM_MSG = 0x9300;

    public static final int T809_UP_WARN_MSG = 0x1400;

    public static final int T809_UP_WARN_MSG_LIST = 0x14000; // 报警处理结果主动上报(批量)

    public static final int T809_DOWN_WARN_MSG = 0x9400;

    public static final int T809_DOWN_PREVENTION_MSG = 0x9C00;

    public static final int T809_UP_PREVENTION_MSG = 0x1C00;

    public static final int T809_UP_CTRL_MSG = 0x1500;

    public static final int T809_DOWN_CTRL_MSG = 0x9500;

    public static final int T809_UP_BASE_MSG = 0x1600;

    public static final int T809_DOWN_BASE_MSG = 0x9600; // 从链路-静态信息交换消息

    public static final int T809_UP_EXG_MSG_REGISTER = 0x1201;

    public static final int T809_UP_EXG_MSG_REAL_LOCATION = 0x1202;

    public static final int T809_UP_EXG_MSG_HISTORY_LOCATION = 0x1203;

    public static final int T809_UP_EXG_MSG_RETURN_STARTUP_ACK = 0x1205;

    public static final int VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK = 0x1205;

    public static final int VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK_BEFOR = 0x120F;

    public static final int ADAS_UP_EXG_MSG_RETURN_END_ACK = 0x1206;

    public static final int T809_UP_EXG_MSG_RETURN_END_ACK = 0x1206;

    public static final int T809_UP_EXG_MSG_APPLY_FOR_MONITOR_STARTUP = 0x1207;

    public static final int T809_UP_EXG_MSG_APPLY_FOR_MONITOR_END = 0x1208;

    public static final int T809_UP_EXG_MSG_REPAIR_TERMINAL = 0x1211;

    public static final int T809_UP_EXG_MSG_REPAIR_FINISH = 0x1212;

    public static final int T808_REQ_MEDIA_STORAGE_FTP_9208 = 0x9208; // 文件上传指令

    public static final int T808_REQ_MEDIA_STORAGE_8208 = 0x8208; // 文件上传指令（陕标）

    public static final int T808_REQ_MEDIA_STORAGE_9502 = 0x9502; // 文件上传指令（京标）

    /**
     * 下发报警信息 京标9504
     */
    public static final int T808_SEND_ALARM_INFO = 0x9504;

    public static final int T808_RSP_MEDIA_STORAGE_FTP_1208 = 0x1208; // 文件上传完成通知

    public static final int T808_RSP_MEDIA_STORAGE_FILE_9212 = 0x9212; // 川标文件上传完成通知

    public static final int T809_UP_EXG_MSG_APPLY_HISGNSSDATA_REQ = 0x1209;

    /**
     * 4.8.2　驾驶员人证照片更新请求
     */
    public static final int T808_REQ_MEDIA_UPDATE = 0x1507;

    public static final int T809_UP_EXG_MSG_REPORT_DRIVER_INFO_ACK = 0x120A;

    public static final int T809_UP_EXG_MSG_TAKE_EWAYBILL_ACK = 0x120B;

    public static final int T809_UP_EXG_MSG_REPORT_DRIVER_INFO = 0x120C;

    public static final int T809_UP_EXG_MSG_REPORT_EWAYBILL_INFO = 0x120D;

    public static final int T809_DOWN_EXG_MSG_CAR_LOCATION = 0x9202;

    public static final int T809_DOWN_EXG_MSG_HISTORY_ARCOSSAREA = 0x9203;

    public static final int T809_DOWN_EXG_MSG_CAR_INFO = 0x9204;

    public static final int T809_DOWN_EXG_MSG_RETURN_STARTUP = 0x9205;

    public static final int T809_DOWN_EXG_MSG_RETURN_STARTUP_BEFOR = 0x920F;

    public static final int T809_DOWN_EXG_MSG_RETURN_START = 0x9201;

    public static final int T809_DOWN_EXG_MSG_RETURN_END = 0x9206;

    public static final int T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_SARTUP_ACK = 0x9207;

    public static final int T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_END_ACK = 0x9208;

    public static final int T809_DOWN_EXG_MSG_APPLY_HISGNSSDATA_ACK = 0x9209;

    public static final int T809_DOWN_EXG_MSG_REPORT_DRIVER_INFO = 0x920A;

    public static final int T809_DOWN_EXG_MSG_TAKE_EWAYBILL_REQ = 0x920B;

    public static final int T809_UP_PLATFORM_MSG_POST_QUERY_ACK = 0x1301;

    public static final int T809_UP_PLATFORM_MSG_INFO_ACK = 0x1302;

    public static final int T809_UP_ENTERPRISE_ON_DUTY_ACK = 0x1305; // 主链路-平台间信息交互信息-平台查岗应答

    public static final int T809_UP_ENTERPRISE_HANDLE_ACK = 0x1306; // 主链路-平台间信息交互信息-运输企业督办应答

    public static final int T809_DOWN_BASE_MSG_ENTERPRISE_ADDED = 0x9602; // 从链路-静态信息交换消息-补报企业静态信息请求

    /**
     * 0x9607(企业静态信息请求消息/补报企业静态信息请求) -> 0x1607(企业静态信息应答消息/补报企业静态信息应答)
     */
    public static final int T809_ENTERPRISE_STATIC_INFO_REQ = 0x9607;

    public static final int T809_UP_BASE_MSG_ENTERPRISE_ADDED_ACK = 0x1602; // 主链路-静态信息交换消息-补报企业静态信息应答

    /**
     * 四川协议
     * 0x1605(道路运输企业静态信息)
     */
    public static final int T809_SI_CHUAN_ENTERPRISE_STATIC_INFO_SYNC = 0x1605;

    /**
     * 四川协议
     * 0x1606(路运输车辆静态信息)
     */
    public static final int T809_SI_CHUAN_VEHICLE_STATIC_INFO_SYNC = 0x1606;

    /**
     * 四川协议
     * 0x1607(道路运输从业人员静态信息)
     */
    public static final int T809_SI_CHUAN_PROFESSIONAL_STATIC_INFO_SYNC = 0x1607;

    /**
     * 0x1607(企业静态信息应答消息/补报企业静态信息应答)
     */
    public static final int T809_ENTERPRISE_STATIC_INFO_ACK = 0x1607;

    /**
     * 企业静态信息同步
     * 当“809管理/监控对象转发管理”新增了监控对象，该监控对象所属企业静态信息还未上报时，通过该指令同步；
     * 当“809管理/监控对象转发管理”已维护的监控对象，该企业信息进行了修改且修改成功后，通过该指令同步；
     */
    public static final int T809_ENTERPRISE_STATIC_INFO_SYNC = 0x1608;

    /**
     * 主动上报终端安装信息消息
     * 当“809管理/监控对象转发管理”新增了监控对象，通过该指令同步；
     * 当“809管理/监控对象转发管理”已维护的监控对象，该监控对象终端信息进行了修改且修改成功后，通过该指令同步；
     */
    public static final int T809_DEVICE_INSTALL_INFO_SYNC = 0X1240;

    /**
     * 黑龙江809
     * 主动上报终端安装信息消息
     * 当“809管理/监控对象转发管理”新增了监控对象，通过该指令同步；
     * 当“809管理/监控对象转发管理”已维护的监控对象，该监控对象终端信息进行了修改且修改成功后，通过该指令同步；
     */
    public static final int T809_HLJ_DEVICE_INSTALL_INFO_SYNC = 0X1210;

    /**
     * 车辆静态信息同步消息
     * 当“809管理/监控对象转发管理”新增了监控对象，通过该指令同步；
     * 当“809管理/监控对象转发管理”已维护的监控对象，该监控对象信息进行了修改且修改成功后，通过该指令同步；
     */
    public static final int T809_VEHICLE_STATIC_INFO_SYNC = 0x1609;

    public static final int T809_DOWN_PLATFORM_MSG_POST_QUERY_REQ = 0x9301;

    public static final int T809_DOWN_PLATFORM_MSG_INFO_REQ = 0x9302;

    public static final int T809_UP_WARN_MSG_URGE_TODO_ACK = 0x1401;

    public static final int T809_DOWN_ENTERPRISE_ON_DUTY_REQ = 0x9305; // 从链路-平台间信息交互信息-运输企业查岗请求

    public static final int T809_DOWN_ENTERPRISE_HANDLE_REQ = 0x9306; // 从链路-平台间信息交互信息-运输企业督办请求

    public static final int T809_UP_WARN_MSG_ADPT_TODO_INFO = 0x1403;

    public static final int T809_UP_WARN_MSG_FILELIST_ACK = 0x1404;

    public static final int T809_HU_UP_WARN_MSG_FILELIST_ACK = 0x1421;

    public static final int T809_UP_WARN_MSG_CHECK_ACK = 0x1405;

    //9406应答指令 1406
    public static final int T809_UP_WARN_MSG_STATICS_ACK = 0x1406;

    public static final int T809_UP_WARN_MSG_FILELIST_AUTO = 0x1407;

    public static final int T809_UP_WARN_MSG_ADPT_INFO = 0x1402;

    public static final int T809_UP_WARN_MSG_URGE_TODO_ACK_INFO = 0X1411; // 下级监管平台上级平台"上报报警督办应答消息"

    public static final int T809_2019_UP_WARN_MSG_ADPT_TODO_INFO = 0x1412; // 主链路-下级平台向上级平台发送报警信息业务-主动上报报警处理结果消息

    public static final int T809_UP_EXG_MSG_FACE_PHOTO_AUTO = 0X1241;

    public static final int T809_UP_WARN_MSG_URGE_TODO_ACK_ACK = 0X1242;

    public static final int T809_UP_EXG_MSG_FACE_PHOTO_REQ = 0X9242;

    public static final int T809_DOWN_WARN_MSG_URGE_TODO_REQ = 0x9401;

    public static final int T809_DOWN_WARN_MSG_INFORM_TIPS = 0x9402;

    public static final int T809_DOWN_WARN_MSG_EXG_INFORM = 0x9403;

    public static final int T809_DOWN_WARN_MSG_FILELIST_REQ = 0X9404;

    public static final int T809_DOWN_WARN_MSG_CHECK_RE = 0X9405;

    public static final int T809_HU_DOWN_WARN_MSG_FILELIST_REQ = 0X9421;


    /**
     * 冀标 9C01智能视频报警附件目录请求业务
     */
    public static final int T809_DOWN_PREVENTION_MSG_FILELIST_REQ = 0x9C01;

    /**
     * 冀标 1C01智能视频报警附件目录应答
     */
    public static final int T809_UP_PREVENTION_MSG_FILELIST_REQ_ACK = 0x1C01;

    /**
     * 豫标 1C02主动上传智能视频报警附件目录
     */
    public static final int T809_UP_PREVENTION_MSG_FILELIST_REQ = 0x1C02;

    /**
     * 报警统计核查请求消息  9406指令
     */
    public static final int T809_DOWN_WARN_MSG_STATICS_REQ = 0x9406;

    public static final int T809_UP_CTRL_MSG_MONITOR_VEHICLE_ACK = 0x1501;

    public static final int T809_UP_CTRL_MSG_TAKE_PHOTO_ACK = 0x1502;

    public static final int T809_UP_CTRL_MSG_TEXT_INFO_ACK = 0x1503;

    public static final int T809_UP_CTRL_MSG_TAKE_TRAVEL_ACK = 0x1504;

    public static final int T809_UP_CTRL_MSG_EMERGENCY_MONITORING_ACK = 0x1505;

    public static final int T809_DOWN_CTRL_MSG_MONITOR_VEHICLE_REQ = 0x9501;

    public static final int T809_DOWN_CTRL_MSG_TAKE_PHOTO_REQ = 0x9502;

    public static final int T809_DOWN_CTRL_MSG_TEXT_INFO = 0x9503;

    public static final int T809_DOWN_CTRL_MSG_TAKE_TRAVEL_REQ = 0x9504;

    public static final int T809_UP_CTRL_MSG_EMERGENCY_MONITORING_REQ = 0x9505;

    public static final int T808_UP_CTRL_MSG_PHOTO_UPDATE_REQ = 0x9506;

    public static final int T808_UP_CTRL_MSG_PHOTO_UPDATE_ACK = 0x9507;

    public static final int T809_UP_BASE_MSG_VEHICLE_ADDED_ACK = 0x1601;

    public static final int T809_DOWN_BASE_MSG_VEHICLE_ADDED = 0x9601;

    public static final int T809_PLATFORM_ADD = 0x0C0B;

    public static final int T809_PLATFORM_DELETE = 0x0D0B;

    public static final int T809_PLATFORM_EDIT = 0x0F0B;

    public static final int T809_FORWARD_DEVICE_ADD = 0x0C0C;

    public static final int T809_FORWARD_DEVICE_DELETE = 0x0D0C;

    public static final int WEB_ALARM_DATA = 0x0C10;// 报警

    public static final int T809_UP_WARN_MSG_FACE_CHECK_AUTO = 0x1408;

    public static final String WEB_SOCKET_T809_CHECK = "/topic/check";

    public static final String WEB_SOCKET_T809_CHECK_GLOBAL = "/topic/checkGlobal";

    public static final String WEB_SOCKET_T809_INSPECT = "/topic/inspect";

    public static final String WEB_SOCKET_T808_ACK = "/t808_ack";

    public static final String WEB_SOCKET_MONITORING = "/monitoring";

    public static final String WEB_SOCKET_T808_CURRENCY_RESPONSE = "/topic/t808_currency_response";// 通用应答 0x0001

    public static final String WEB_SOCKET_T808_F3_STATUS = "/t808_f3_status_info";// F3状态信息 0x0900

    public static final String WEBSOCKET_REFRESH_LIST = "/topic/fencestatus";// 围栏下发状态

    public static final String WEBSOCKET_FENCE_STATUS = "/topic/fencestatus";// 围栏下发状态

    public static final String WEBSOCKET_ADAS_PARAM_STATUS = "/adasParam";// 主动安全参数下发状态

    public static final String WEBSOCKET_REAL_LOCATION_P = "/topic/realLocationP";// 围栏下发状态 /**

    public static final String WEBSOCKET_DEVICE_REPORT_LOG_TOPIC = "/topic/deviceReportLog";// 终端上报生成日志

    public static final String WEBSOCKET_DEVICE_REPORT_LOG_USER = "/topic/deviceReportLog";// 终端上报生成日志

    public static final String F3_HIGH_PRECISION_ALARM = "/topic/highPrecisionAlarm";// 终端上报生成日志

    public static final String WEB_SOCKET_T808_ALARM = "/topic/alarm"; // 报警

    public static final String WEB_SOCKET_GLOBAL_ALARM = "/topic/alarmGlobal"; // 报警

    /**
     * 两客一危界面全局报警订阅'/topic/alarmGlobal' 收不到全局报警 所以新增一个地址
     */
    public static final String WEB_SOCKET_LKYW_GLOBAL_ALARM = "/topic/lkywGlobalAlarm";

    public static final String WEB_SOCKET_GLOBAL_ALARM_HANDLE_NOTICE = "/topic/globalAlarmHandleNotice"; // 报警

    public static final String WEB_SOCKET_T808_STATUS = "/topic/cachestatus"; // 状态信息

    public static final String WEB_SOCKET_T808_LOCATION = "/topic/location"; // 位置数据

    public static final String WEB_SOCKET_OBD_URL = "/topic/obdInfo";

    public static final String WEB_SOCKET_RISK_LOCATION = "/topic/riskInfo"; // 位置数据

    public static final String WEB_SOCKET_T808_ADASLOCATION = "/topic/adasLocation"; // 位置数据

    public static final String WEB_SOCKET_T808_MEDIAINFO = "/topic/mediainfo"; // 音视频数据

    public static final String WEB_SOCKET_T808_AUTO = "/autoinfo"; // 音频数据

    public static final String WEB_SOCKET_SPECIAL_REPORT = "/topic/specialReport";//特殊报警

    public static final String WEB_SOCKET_HEALTH = "/topic/health";//心跳

    public static final String WEB_SOCKET_REMOTE_UPGRADE_TYPE = "/topic/remoteUpgradeType";//远程升级状态

    public static final String WEBSOCKET_DEVICE_REPORT_TOPIC = "/topic/deviceProperty";// 终端属性

    public static final String WEB_SOCKET_T809_OFFLINE_RECONNECT = "/topic/T809OfflineReconnect";//809断线重连消息

    public static final String WEB_SOCKET_PLATFORM_REMIND = "/topic/monitor/platformRemind"; // 平台报警提醒

    public static final String WEB_SOCKET_TOPIC_PLATFORM_REMIND = "/topic/platformRemind"; // 平台报警提醒

    public static final String WEB_SOCKET_SOS_ALARM = "/sosAlarm"; // 报警

    /**
     * 离线报表导出
     */
    public static final String WEB_SOCKET_OFFLINE_EXPORT = "/topic/offlineExport";

    /**
     * 导入进度推送
     */
    public static final String WEB_SOCKET_IMPORT_PROGRESS = "/topic/import/progress";

    /**
     * 终端唤醒
     */
    public static final String WEBSOCKET_DEVICE_WAKE_UP = "/topic/device/wakeUp";

    /**
     * 油补 结果
     */
    public static final String WEBSOCKET_OIL_SUPPLEMENT_RESULT = "/topic/oilSupplement/sendReissueDataRequest";

    /**
     * io监测数据控制 8500 -> 0500
     */
    public static final String WEBSOCKET_OUTPUT_CONTROL = "/topic/ioMonitoring/send8500";

    // F3-超长待机
    public static final int F3_CLBS_SET_PARAM = 0xF38103;

    public static final int F3_CLBS_SET_TRACE = 0xF38202;

    // ASO-超长待机-上传定点
    public static final int ASO_CLBS_FIXED_POINT = 0xA89219;

    // ASO-超长待机-透传命令
    public static final int ASO_CLBS_TRANSPARENT = 0xA8920A;

    // ASO-超长待机-上传频率
    public static final int ASO_CLBS_FREQUENCY = 0xA89218;

    // ASO-超长待机-复位重启
    public static final int ASO_CLBS_RESTART = 0xA8921A;

    // 博实结-超长待机
    public static final int BSJ_CLBS_SET_PARAM = 0xB538103;

    // 状态缓存
    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_STATUS_INTO = 0x9999;// BS端请求车辆缓存状态信息

    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_ADD_INTO = 0x8888;// BS端请求车辆增加缓存状态信息

    public static final int BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO = 0x7777;// BS端请求车辆更新缓存状态信息

    // 手咪设备
    public static final int BDTD_LOCATION = 0xB3;// 手环位置信息

    public static final int BDTD_ALARM = 0xB4;// 手环位置信息

    // 移位位置信息
    public static final int DEVICE_GV_LOCATION_INFO = 0x1043;// gv位置信息

    // 天禾设备
    public static final int DEVICE_TH_LOCATION_INFO = 0x3060;// th位置信息

    public static final int WEB_SUBSCRIPTION_ADD = 0x0C08;// 增加订阅

    public static final int WEB_SUBSCRIPTION_REMOVE = 0x0D08;// 删除订阅

    public static final int WEB_ALARM_ADD = 0x0C0A;// 增加报警设置

    public static final int WEB_ALARM_REMOVE = 0x0D0A;// 删除报警设置

    public static final int OPEN_AUDIO_VIDEO = 0xFE02;// 开启实时音视频

    public static final int CLOSE_AUDIO_VIDEO = 0xFE03;// 关闭实时音视频

    public static final int AUDIO_VIDEO_INFO = 0xFE01;// 实时音视频数据

    public static final int AUDIO_VIDEO_FLAG = 0xFE04;// 音视频开启关闭标识

    public static final int SUSPENSION_OF_AUDIO_AND_VIDEO_TRANSMISSION = 0xFE05;// 暂停音视频传输

    public static final int AUDIO_AND_VIDEO_TRANSMISSION = 0xFE06;// 恢复音视频传输

    public static final int VIDEO_FLOW = 0xFFDD; // 音视频流量

    public static final int FTP_DISK_INFO = 0xFFDE; // 磁盘信息

    public static final int FTP_VIDEO_SAVE = 0xFFDF; // FTP文件存储

    /**
     * 监控对象单次流量消耗情况 51001
     */
    public static final int VIDEO_MONITOR_FLOW_CONSUMPTION = 0xC739;

    /**
     * 用户单次流量消耗情况 51002
     */
    public static final int VIDEO_USER_FLOW_CONSUMPTION = 0XC73A;

    /**
     * 用户开始播放视频 51003
     */
    public static final int VIDEO_USER_BEGIN_PLAY = 0XC73B;

    // 云台设备
    public static final int T808_ROTATING_CONTROL = 0x9301;// 云台旋转控制 0x9301

    public static final int T808_FOCAL_LENGTH_CONTROL = 0x9302;// 云台调整焦距控制 0x9302

    public static final int T808_APERTURE_CONTROL = 0x9303;// 云台调整光圈控制

    public static final int T808_WIPER_CONTROL = 0x9304;// 云台雨刷控制

    public static final int T808_INFRARED_FILL_LIGHT_CONTROL = 0x9305;// 红外补光控制

    public static final int T808_ZOOM_CONTROL = 0x9306;// 云台变倍控制

    private static final Map<String, String> deviceFunctionTypeMap = Maps.newHashMap();

    // 实时视频指令下发
    public static final int VIDEO_REQUEST = 0x9101; // 实时音视频传输请求

    public static final int VIDEO_CONTROL = 0x9102; // 实时音视频传输控制

    public static final int VIDEO_ATTRIBUTE_QUERY = 0x9003; // 查询音视频属性

    public static final int VIDEO_CHANNEL_SETTING = 0x0076; // 查询音视频属性

    public static final int LOSS_RATE = 0x9105; //丢包率

    public static final String FILE_UPLOAD_STATUS = "/topic/fileUploadStatus"; // 文件上传通知

    public static final int DEVICE_UPLOAD_RIDERSHIP = 0x1005;//终端上传乘客流量

    public static final int DEVICE_UPLOAD_VIDEO_PARAM = 0x1003;//终端上传音视频属性

    public static final int STRESS_TEST_VIDEO = 0xFE07;// 压测请求实时视频

    public static final String WEB_REMOTE_UPGRADE = "/topic/remoteUpgrade"; // 远程升级

    // 接受督导高敏
    public static final int RISK_RESULT_ACCEPT = 1;

    // 接受督导低敏
    public static final int RISK_RESULT_REJECT = 2;

    // 事故发生
    public static final int RISK_RESULT_ACCIDENT = 3;

    // 未归档
    public static final int RISK_RESULT_UNTREATED = 4;

    /**
     * 设置省域ID
     */
    public static final int SETTING_PROVINCE_ID = 0X0081;

    /**
     * 设置市域ID
     */
    public static final int SETTING_CITY_ID = 0X0082;

    /**
     * 设置车牌号
     */
    public static final int SETTING_PLATE_NUMBER = 0X0083;

    /**
     * 设置车牌颜色
     */
    public static final int SETTING_PLATE_COLOR = 0X0084;

    /**
     * 驾驶员身份识别上报
     */
    public static final int DRIVER_IDENTIFICATION_REPORT = 0x0E10;

    /**
     * 终端驾驶员身份库查询指令下发
     */
    public static final int QUERY_DEVICE_DRIVER_REQ = 0x8E12;

    /**
     * 终端驾驶员身份库查询应答
     */
    public static final int QUERY_DEVICE_DRIVER_REQ_ACK = 0x0E12;

    /**
     * 终端驾驶员身份库下发指令下发
     */
    public static final int ISSUE_DEVICE_DRIVER_DISCERN = 0x8E11;


    /**
     * 终端驾驶员身份库同步下发指令下发
     */
    public static final int ISSUE_DEVICE_DRIVER_SYNCHRONIZE = 0x8E21;


    /**
     * 终端驾驶员身份库查询指令下发
     */
    public static final int QUERY_DEVICE_DRIVER_REQ_HUNAN = 0x8E22;

    /**
     * 终端驾驶员身份库查询应答
     */
    public static final int QUERY_DEVICE_DRIVER_REQ_HUNAN_ACK = 0x0E22;
    /**
     * 上级平台巡检下级平台平台日志应答
     */
    public static final int UP_PLATFORM_MSG_INSPECTION_LOG_ACK = 0x1311;
    /**
     *上级平台巡检下级平台平台日志请求
     */
    public static final int DOWN_PLATFORM_MSG_INSPECTION_LOG_REQ = 0x9311;

    /**
     * 上上级平台巡检下级平台监控人员应答
     */
    public static final int UP_PLATFORM_MSG_INSPECTION_USER_ACK = 0x1310;
    /**
     *上级平台巡检下级平台监控人员请求
     */
    public static final int DOWN_PLATFORM_MSG_INSPECTION_USER_REQ = 0x9310;

    /**
     * 下发车辆行车路线信息消息
     */
    public static final int DOWN_EXG_MSG_DRVLINE_INFO = 0x9211;

    public static final String TERMINAL_IO = "terminalIo";

    public static final String SENSOR_IO = "sensorIo";

    public static final String ALL_IO = "allIo";

    public static final String PREFIX_STATUS = "0_";
    public static final String PREFIX_POSITION = "1_";
    public static final String PREFIX_MSG_ACK = "2_";
    public static final String PREFIX_DEVICE_ALARM = "3_";
    public static final String PREFIX_MONITOR_ALARM = "4_";

    /**
     * 驾驶员主动上报
     */
    public static final int DEVICE_ACTIVE_REPORT = 0x8702;

    static {
        // 功能类型
        deviceFunctionTypeMap.put("1", "简易型车机");
        deviceFunctionTypeMap.put("2", "行车记录仪");
        deviceFunctionTypeMap.put("3", "对讲设备");
        deviceFunctionTypeMap.put("4", "手咪设备");
        deviceFunctionTypeMap.put("5", "超长待机设备");
        deviceFunctionTypeMap.put("6", "定位终端");

    }

    public static String getDeviceProtocolType(String deviceProtocolId) {
        return ProtocolEnum.getDeviceNameByDeviceType(deviceProtocolId);
    }

    public static String getDeviceFunctionType(String deviceFunctionId) {
        return deviceFunctionTypeMap.getOrDefault(deviceFunctionId, "");
    }


    public static String getRoadName(Integer roadType) {
        if (roadType == null) {
            return null;
        }
        switch (roadType) {
            case 0x01:
                return "高速路";
            case 0x02:
                return "都市高速路";
            case 0x03:
                return "国道";
            case 0x04:
                return "省道";
            case 0x05:
                return "县道";
            case 0x06:
                return "乡村道路";
            case 0x07:
                return "其他道路";
            default:
                return null;
        }
    }

    /**
     * 调度报警
     */
    public static final List<Integer> DISPATCH_ALARM_TYPE_LIST = Arrays.asList(152, 153, 154, 155, 156);

    /**
     * 809-2019版本 协议类型
     */
    public static final Set<Integer> CONNECT_PROTOCOL_TYPE_808_2019 = Sets.newHashSet(100, 1011, 1012, 1013, 1091);
}

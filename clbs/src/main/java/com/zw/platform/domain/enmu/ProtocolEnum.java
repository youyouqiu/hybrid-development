package com.zw.platform.domain.enmu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 协议类型转换
 * 注:
 * 1.前端有些地方也需要修改;
 * 2.{@link com.zw.platform.controller.monitoring.TrackPlaybackController} 中,
 * getTodayFirstData()和changeHistoryActiveDate()方法中需要增加对应的协议类型(deviceType)
 * 3.如有其它地方还有, 需补充到此处, 避免后续增加协议, 到处找!SALUTE!
 *
 * @author zhouzongbo on 2019/6/20 16:14
 */
public enum ProtocolEnum {
    // 1: 协议ENUM
    T808_2011(ProtocolEnum.ONE, "0", "交通部JT/T808-2011(扩展)"),
    T808_2013(ProtocolEnum.ONE, "1", "交通部JT/T808-2013"),
    YW(ProtocolEnum.THREE, "2", "移为"),
    TH(ProtocolEnum.THREE, "3", "天禾"),
    BD_TD(ProtocolEnum.THREE, "5", "BDTD-SM"),
    KKS(ProtocolEnum.THREE, "6", "KKS"),
    KKS_WIRELESS(ProtocolEnum.THREE, "7", "康凯斯无线"),
    BSJ(ProtocolEnum.TWO, "8", "BSJ-A5"),
    ASO(ProtocolEnum.ONE, "9", "ZYM"),
    F3_LONG_STANDBY(ProtocolEnum.ONE, "10", "F3超长待机"),
    T808_2019(ProtocolEnum.ONE, "11", "交通部JT/T808-2019"),
    ADAS_SC_2013(ProtocolEnum.ONE, "12", "交通部JT/T808-2013(川标)"),
    ADAS_HB_2019(ProtocolEnum.ONE, "13", "交通部JT/T808-2013(冀标)"),
    ADAS_GL_2013(ProtocolEnum.ONE, "14", "交通部JT/T808-2013(桂标)"),
    ADAS_JS_2013(ProtocolEnum.ONE, "15", "交通部JT/T808-2013(苏标)"),
    ADAS_ZJ_2013(ProtocolEnum.ONE, "16", "交通部JT/T808-2013(浙标)"),
    ADAS_JL_2013(ProtocolEnum.ONE, "17", "交通部JT/T808-2013(吉标)"),
    ADAS_SX_2013(ProtocolEnum.ONE, "18", "交通部JT/T808-2013(陕标)"),
    ADAS_JX_2013(ProtocolEnum.ONE, "19", "交通部JT/T808-2013(赣标)"),
    ADAS_SH_2019(ProtocolEnum.ONE, "20", "交通部JT/T808-2019(沪标)"),
    T808_2019_ZW(ProtocolEnum.ONE, "21", "交通部JT/T808-2019(中位)"),
    KKS_EV25(ProtocolEnum.THREE, "22", "KKS-EV25"),
    T808_2011_1078(ProtocolEnum.ONE, "23", "JT/T808-2011(1078报批稿)"),
    T808_2019_BJ(ProtocolEnum.ONE, "24", "交通部JT/T808-2019(京标)"),
    T808_2019_HLJ(ProtocolEnum.ONE, "25", "交通部JT/T808-2019(黑标)"),
    T808_2019_SD(ProtocolEnum.ONE, "26", "交通部JT/T808-2019(鲁标)"),
    T808_2013_HN(ProtocolEnum.ONE, "27", "交通部JT/T808-2013(湘标)"),
    T808_2019_GD(ProtocolEnum.ONE, "28", "交通部JT/T808-2019(粤标)"),
    OBD_GB_2018(ProtocolEnum.ONE, "97", "OBD-GB-2018"),
    OBD_HZ_2018(ProtocolEnum.ONE, "99", "OBD-杭州-2018"),
    ;


    /**
     * 信息配置和终端导入时用于校验导入的设备类型是否正确
     */
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;

    /**
     * 如果传感器协议类型为808-2013, 查询数据时, 也需要查询808-2013川标和冀标内容
     */
    public static final Integer[] PROTOCOL_TYPE_808_2013 = {1, 12, 13, 14, 15, 16, 17, 18, 19, 27};

    /**
     * 808所有协议类型
     */
    public static final Integer[] PROTOCOL_TYPE_808 =
        {0, 1, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 28};

    public static final String[] PROTOCOL_TYPE_808_2013_STR =
        {"1", "12", "13", "14", "15", "16", "17", "18", "19", "27"};

    public static final String[] PROTOCOL_TYPE_808_2011_2013_STR
            = {"0", "1", "12", "13", "14", "15", "16", "17", "18", "19", "23", "27"};

    public static final String[] PROTOCOL_TYPE_808_2019_STR = {"11", "20", "21", "24", "25", "26", "28"};
    /**
     * 如果传感器协议类型为808-2019, 查询数据时也需要查询相同年份的协议, 后续仅在这个数组中增加相同类型即可
     */
    public static final Integer[] PROTOCOL_TYPE_808_2019 = {11, 20, 21, 24, 25, 26, 28};
    /**
     * 实时视频终端协议类型
     */
    public static final String[] REALTIME_VIDEO_DEVICE_TYPE  = {"1", "11", "12", "13", "14", "15", "16", "17", "18",
        "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"};

    /**
     * 能在报表中查询出来的协议类型
     */
    public static final String[] REPORT_DEVICE_TYPE
            = {"1", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "24", "25", "26", "27", "28"};

    /**
     * 实时监控心跳终端协议类型
     */
    public static final String[] REALTIME_MONITOR_HEART_DEVICE_TYPE = {"1", "2", "6", "9"};

    private static final Map<String, String> DEVICE_NAME_AND_TYPE_MAP = new HashMap<>(16);
    public static final Map<String, String> DEVICE_TYPE_AND_NAME_MAP = new HashMap<>(16);
    public static final Map<String, Integer> DEVICE_TYPE_AND_SIGN = new HashMap<>(16);
    public static final Set<String> DEVICE_TYPE_NAMES = new LinkedHashSet<>();

    // 初始化基础数据
    static {
        ProtocolEnum[] values = ProtocolEnum.values();
        for (ProtocolEnum value : values) {
            Integer sign = value.getSign();
            String deviceType = value.getDeviceType();
            String deviceTypeName = value.getDeviceTypeName();
            DEVICE_NAME_AND_TYPE_MAP.put(deviceTypeName, deviceType);
            DEVICE_TYPE_AND_NAME_MAP.put(deviceType, deviceTypeName);
            DEVICE_TYPE_AND_SIGN.put(deviceType, sign);
            if (!"7".equals(deviceType)) {
                DEVICE_TYPE_NAMES.add(deviceTypeName);
            }
        }
    }

    /**
     * 1: 使用sim卡作为唯一标识
     * 2: 将sim卡号转换为伪IP
     * 3: 其他
     * 用于平台判断执行那种算法
     */
    private final Integer sign;
    /**
     * 设备类型:0:交通部JT/T808-2011(扩展);1:交通部JT/T808-2013;2:移为GV320;
     * 3:天禾;5:北斗天地协议;8:博实结;9:ASO;10:F3超长待机;11:808-2019;40:809-2019
     */
    private final String deviceType;
    /**
     * 设备类型名称
     */
    private final String deviceTypeName;

    ProtocolEnum(Integer sign, String deviceType, String deviceTypeName) {
        this.sign = sign;
        this.deviceType = deviceType;
        this.deviceTypeName = deviceTypeName;
    }

    public Integer getSign() {
        return sign;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    /**
     * 根据DeviceName获取DeviceType
     *
     * @param deviceName deviceName
     * @return deviceType
     */
    public static String getDeviceTypeByDeviceName(String deviceName) {
        return DEVICE_NAME_AND_TYPE_MAP.getOrDefault(deviceName, "");
    }

    /**
     * 根据deviceType获取DeviceName
     *
     * @param deviceType deviceType
     * @return 协议类型
     */
    public static String getDeviceNameByDeviceType(String deviceType) {
        return DEVICE_TYPE_AND_NAME_MAP.getOrDefault(deviceType, "");
    }

    /**
     * 根据deviceType获取标识
     *
     * @param deviceType deviceType
     * @return sign
     */
    public static Integer getSignByDeviceType(String deviceType) {
        return DEVICE_TYPE_AND_SIGN.getOrDefault(deviceType, ProtocolEnum.THREE);
    }

    public static List<Integer> getProtocols(Integer protocol) {
        List<Integer> protocols = new ArrayList<>();
        switch (protocol) {
            case 1:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 27:
                // 808-2013版本
                protocols = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013);
                break;
            case 11:
            case 20:
            case 21:
            case 24:
            case 25:
            case 26:
            case 28:
                // 808-2019版本
                protocols = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019);
                break;
            default:
                protocols.add(protocol);
                break;
        }
        return protocols;
    }

    public static List<String> getProtocolsTree() {
        List<String> protocols;
        protocols = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
        return protocols;
    }

    public static List<String> getProtocols808_2103() {
        List<String> protocols;
        protocols = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013_STR);
        return protocols;
    }

    /**
     * 根据协议类型获取通讯类型
     * @param deviceType 协议类型
     */
    public static List<Integer> getProtocolTypes(Integer deviceType) {
        List<Integer> deviceTypes = new ArrayList<>();
        if (deviceType == -1) {
            deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013);
        } else if (deviceType == 11) {
            deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019);
        } else {
            deviceTypes.add(deviceType);
        }
        return deviceTypes;
    }
}

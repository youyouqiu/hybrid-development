package com.zw.app.domain.alarm;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/30 10:43
 */
public enum AlarmSetTypeAndAlarmTypeMappingRelationEnum {

    /**
     * 交通部JT/T808 -> 驾驶员引起报警 -> 进出区域
     */
    T808_DRIVER_ENTER_OR_OUT_AREA("20", "进出区域", Sets.newHashSet("20", "2011", "2012")),
    /**
     * 交通部JT/T808 -> 驾驶员引起报警 -> 进出线路
     */
    T808_DRIVER_ENTER_OR_OUT_LINE("21", "进出线路", Sets.newHashSet("21", "2111", "2112")),
    /**
     * 交通部JT/T808 -> 驾驶员引起报警 -> 路段行驶时间不足/过长
     */
    T808_DRIVER_ROAD_TRAVEL_TIME_NOT_ENOUGH_OR_TO_LONG("22", "路段行驶时间不足/过长", Sets.newHashSet("22", "2211", "2212")),
    /**
     * 交通部JT/T808 -> F3高精度报警 -> 设备电量报警
     */
    T808_HIGH_PRECISION_ALARM_DEVICE_POWER_ALARM("18811", "设备电量报警",
        Sets.newHashSet("18810", "18811", "18812", "18813", "18814", "18815")),
    /**
     * 交通部JT/T808 -> F3高精度报警 -> 加速度传感器异常报警
     */
    T808_HIGH_PRECISION_ALARM_ACCELERATION_SENSOR_ABNORMAL_ALARM("18715", "加速度传感器异常报警",
        Sets.newHashSet("18715", "18718")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 温度报警
     */
    T808_SENSOR_TEMPERATURE_ALARM("6511", "温度报警",
        Sets.newHashSet("6511", "6512", "6513", "6521", "6522", "6523", "6531", "6532", "6533", "6541", "6542", "6543",
            "6551", "6552", "6553")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 湿度报警
     */
    T808_SENSOR_HUMIDITY_ALARM("6611", "湿度报警",
        Sets.newHashSet("6611", "6612", "6613", "6621", "6622", "6623", "6631", "6632", "6633", "6641", "6642", "6643",
            "6651", "6652", "6653")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 超速报警(F3)
     */
    T808_SENSOR_OVER_SPEED_ALARM_F3("67", "超速报警(F3)", Sets.newHashSet("67", "14411")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 加漏油报警
     */
    T808_SENSOR_REFUELING_LEAKAGE_OIL_ALARM("6811", "加漏油报警",
        Sets.newHashSet("6811", "6812", "6813", "6821", "6822", "6823", "6831", "6832", "6833", "6841", "6842",
            "6843")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 胎压报警
     */
    T808_SENSOR_TIRE_PRESSURE_ALARM("14300", "胎压报警",
        Sets.newHashSet("14300", "14301", "14302", "14303", "14304", "14305", "14306", "14310", "143100", "143101",
            "143102", "143103", "143104", "143105", "143106", "14311", "143110", "143111", "143112", "143113", "143114",
            "143115", "143116", "14312", "143120", "143121", "143122", "143123", "143124", "143125", "143126", "14313",
            "143130", "143131", "143132", "143133", "143134", "143135", "143136", "14314", "143140", "143141", "143142",
            "143143", "143144", "143145", "143146", "14315", "143150", "143151", "143152", "143153", "143154", "143155",
            "143156", "14316", "143160", "143161", "143162", "143163", "143164", "143165", "143166", "143170", "143171",
            "143172", "143173", "143174", "143175", "143176", "143180", "143181", "143182", "143183", "143184",
            "143185", "143186", "143190", "143191", "143192", "143193", "143194", "143195", "143196", "14320", "14321",
            "14322", "14323", "14324", "14325", "14326", "14330", "14331", "14332", "14333", "14334", "14335", "14336",
            "14340", "14341", "14342", "14343", "14344", "14345", "14346", "14350", "14351", "14352", "14353", "14354",
            "14355", "14356", "14360", "14361", "14362", "14363", "14364", "14365", "14366", "14370", "14371", "14372",
            "14373", "14374", "14375", "14376", "14380", "14381", "14382", "14383", "14384", "14385", "14386", "14390",
            "14391", "14392", "14393", "14394", "14395", "14396", "14399")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 反转报警
     */
    T808_SENSOR_REVERSE_ALARM("124", "反转报警", Sets.newHashSet("124", "12411")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 工时报警
     */
    T808_SENSOR_WORKING_HOURS_ALARM("13213", "工时报警", Sets.newHashSet("13213", "13214")),
    /**
     * 交通部JT/T808 -> F3传感器报警 -> 载重报警
     */
    T808_SENSOR_LOAD_ALARM("7012", "载重报警", Sets.newHashSet("7011", "7012", "7021", "7022")),
    /**
     * 交通部JT/T808 -> 平台报警 -> 进出区域
     */
    T808_PLATE_ENTER_OR_OUT_AREA("7211", "进出区域", Sets.newHashSet("7211", "7212")),
    /**
     * 交通部JT/T808 -> 平台报警 -> 进出线路
     */
    T808_PLATE_ENTER_OR_OUT_LINE("7311", "进出线路", Sets.newHashSet("7311", "7312")),
    /**
     * 交通部JT/T808 -> 平台报警 -> 关键点报警
     */
    T808_PLATE_KEY_POINT_ALARM("11911", "关键点报警", Sets.newHashSet("11911", "11912")),
    /**
     * 交通部JT/T808 -> 平台报警 -> 路线偏离报警
     */
    T808_PLATE_ROUTE_DEPARTURE_ALARM("75", "路线偏离报警", Sets.newHashSet("75", "147")),
    /**
     * 交通部JT/T808 -> 平台报警 -> 异动报警
     */
    T808_PLATE_ABNORMAL_CHANGE_ALARM("77", "异动报警", Sets.newHashSet("77", "7702", "7703")),
    /**
     * BDTD-SM -> 平台报警 -> 进出区域
     */
    BDTD_SM_PLATE_ENTER_OR_OUT_AREA("11511", "进出区域", Sets.newHashSet("11511", "11512")),
    /**
     * BDTD-SM -> 平台报警 -> 进出线路
     */
    BDTD_SM_PLATE_ENTER_OR_OUT_LINE("11611", "进出线路", Sets.newHashSet("11611", "11612")),
    /**
     * ASO -> ASO -> 拆机报警
     */
    ASO_ASO_DISASSEMBLY_ALARM("1111", "拆机报警", Sets.newHashSet("1111", "1112")),
    ;

    private static final Map<String, Set<String>> ALARM_SET_TYPE_AND_ALARM_TYPE_MAP = new HashMap<>(16);
    private static final Map<String, String> ALARM_SET_TYPE_AND_ALARM_TYPE_NAME_MAP = new HashMap<>(16);

    static {
        for (AlarmSetTypeAndAlarmTypeMappingRelationEnum value : AlarmSetTypeAndAlarmTypeMappingRelationEnum.values()) {
            String alarmSettingType1 = value.getAlarmSettingType();
            ALARM_SET_TYPE_AND_ALARM_TYPE_MAP.put(alarmSettingType1, value.getAlarmTypeSet());
            ALARM_SET_TYPE_AND_ALARM_TYPE_NAME_MAP.put(alarmSettingType1, value.getAlarmSettingName());
        }
    }

    private final String alarmSettingType;
    private final String alarmSettingName;
    private final Set<String> alarmTypeSet;

    AlarmSetTypeAndAlarmTypeMappingRelationEnum(String alarmSettingType, String alarmSettingName,
        Set<String> alarmTypeSet) {
        this.alarmSettingType = alarmSettingType;
        this.alarmSettingName = alarmSettingName;
        this.alarmTypeSet = alarmTypeSet;
    }

    public String getAlarmSettingType() {
        return alarmSettingType;
    }

    public Set<String> getAlarmTypeSet() {
        return alarmTypeSet;
    }

    public String getAlarmSettingName() {
        return alarmSettingName;
    }

    public static String getAlarmSettingTypeByAlarmType(String alarmType) {
        for (Map.Entry<String, Set<String>> entry : ALARM_SET_TYPE_AND_ALARM_TYPE_MAP.entrySet()) {
            if (entry.getValue().contains(alarmType)) {
                return entry.getKey();
            }
        }
        return alarmType;
    }

    public static Set<String> getAlarmTypeSetByAlarmSettingType(String alarmSettingType) {
        return ALARM_SET_TYPE_AND_ALARM_TYPE_MAP.getOrDefault(alarmSettingType, Sets.newHashSet(alarmSettingType));
    }

    public static String getAlarmSettingNameByAlarmSettingType(String alarmSettingType, String defaultValue) {
        return ALARM_SET_TYPE_AND_ALARM_TYPE_NAME_MAP.getOrDefault(alarmSettingType, defaultValue);
    }

}

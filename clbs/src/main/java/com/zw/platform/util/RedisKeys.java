package com.zw.platform.util;

import com.zw.platform.basic.constant.RedisKeyEnum;

import static com.zw.platform.basic.constant.RedisKeyEnum.LOAD_SETTING_MONITORY_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.OBD_SETTING_MONITORY_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.TYRE_PRESSURE_MONITORY_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_OIL_SENSOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_ROTATE_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_TEMPERATURE_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.VEHICLE_WET_MONITOR_LIST;
import static com.zw.platform.basic.constant.RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST;

/**
 * Created by PengFeng on 2017/9/30 16:32
 * @deprecated  since 4.4.0 replace RedisKeyEnum
 */
public final class RedisKeys {

    private RedisKeys() {
        // 限制初始化
    }

    /**
     * 车辆-设备-SIM卡模糊查询 key
     */
    public static final String VEHICLE_DEVICE_SIMCARD_FUZZY_SEARCH = "vehicle_device_simcard_fuzzy_search";

    /**
     * 配置信息顺序key
     */
    public static final String SORT_CONFIG_LIST = "sort_config_list";

    /**
     * 车辆顺序
     */
    public static final String SORT_VEHICLE_LIST = "sort_vehicle_list";

    /**
     * 物品顺序
     */
    public static final String SORT_THING_LIST = "sort_thing_list";

    /**
     * 人员顺序
     */
    public static final String SORT_PEOPLE_LIST = "sort_people_list";

    /**
     * 设备顺序
     */
    public static final String SORT_DEVICE_LIST = "sort_device_list";

    /**
     * SIM卡顺序
     */
    public static final String SORT_SIMCARD_LIST = "sort_simcard_list";

    public static final String SORT_PROFESSIONAL_LIST = "sort_professional_list";

    public static final String NAME_IDENTITY_STATE_FUZZY_SEARCH = "name_identity_state_fuzzy_search";

    public static final String SEPARATOR = "#@!@#";

    /**
     * 终端报警指令
     */
    public static final String DEVICE_ALARM_INSTRUCT_LIST = "device_alarm_instruct_list";

    /**
     * 企业及其下级企业uuid维护
     */
    public static final String ORG_CHILD_UUID_LIST = "org_child_uuid_list";

    /**
     * 企业下的车辆树
     */
    public static final String ORG_VEHICLE_NUM = "org_vehicle_num";

    /**
     * 车辆当天报警类型key
     */
    public static final String TIME_MONITOR_ALARM = "%s:%s:%s";

    /**
     * 车辆当天报警处理情况key（处理总数和下发短信处理的条数）
     */
    public static final String TIME_MONITOR_DEAL_MSG = "%s:%s";

    public static class SensorType {

        public static final String SENSOR_WET_MONITOR = "wet_monitor";

        public static final String SENSOR_TEMPERATURE_MONITOR = "temperature_monitor";

        public static final String SENSOR_OIL_BOX_MONITOR = "oil_box_monitor";

        public static final String SENSOR_OIL_CONSUMER_MONITOR = "oil_consumer_monitor";

        public static final String SENSOR_SHOCK_MONITOR = "shock_monitor";

        public static final String SENSOR_ROTATE_MONITOR = "rotate_monitor";

        public static final String SENSOR_MILEAGE_MONITOR = "mileage_monitor";

        public static final String SENSOR_WORK_HOUR_MONITOR = "work_hour_monitory";

        public static final String SENSOR_LOAD_MONITOR = "load_monitory";

        public static final String SENSOR_OBD_MONITOR = "obd_monitory";

        public static final String SENSOR_TYRE_PRESSURE_MONITOR = "tyre_pressure_monitory";

        public static final String SENSOR_TYRE_OIL_MONITOR = "oil_monitor";

        /**
         * 实际上没必要用这个type，但目前只处理重要问题
         */
        public static final Translator<RedisKeyEnum, String> SENSOR_KEY = Translator.<RedisKeyEnum, String>builder()
                .add(VEHICLE_OIL_BOX_MONITOR_LIST, SENSOR_OIL_BOX_MONITOR)
                .add(VEHICLE_TEMPERATURE_MONITOR_LIST, SENSOR_TEMPERATURE_MONITOR)
                .add(VEHICLE_WET_MONITOR_LIST, SENSOR_WET_MONITOR)
                .add(VEHICLE_OIL_CONSUME_MONITOR_LIST, SENSOR_OIL_CONSUMER_MONITOR)
                .add(VEHICLE_ROTATE_MONITOR_LIST, SENSOR_ROTATE_MONITOR)
                .add(VEHICLE_SHOCK_MONITOR_LIST, SENSOR_SHOCK_MONITOR)
                .add(VEHICLE_MILEAGE_MONITOR_LIST, SENSOR_MILEAGE_MONITOR)
                .add(WORK_HOUR_SETTING_MONITORY_LIST, SENSOR_WORK_HOUR_MONITOR)
                .add(LOAD_SETTING_MONITORY_LIST, SENSOR_LOAD_MONITOR)
                .add(OBD_SETTING_MONITORY_LIST, SENSOR_OBD_MONITOR)
                .add(TYRE_PRESSURE_MONITORY_LIST, SENSOR_TYRE_PRESSURE_MONITOR)
                .add(VEHICLE_OIL_SENSOR_LIST, SENSOR_TYRE_OIL_MONITOR)
                .build();
    }

}

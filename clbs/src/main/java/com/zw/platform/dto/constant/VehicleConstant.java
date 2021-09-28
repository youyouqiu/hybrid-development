package com.zw.platform.dto.constant;

import com.zw.platform.util.Translator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 车辆相关常量
 * @author create by zhouzongbo on 2020/9/8.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VehicleConstant {

    /**
     * 监控对象类型翻译
     */
    public static final Translator<String, String> MONITOR_TYPE_TRANSLATOR =
        Translator.of("车", "0", "人", "1", "物", "2");

    public static final String MONITOR_TYPE_VEHICLE = "0";
    public static final String MONITOR_TYPE_PEOPLE = "1";
    public static final String MONITOR_TYPE_THING = "2";

    /**
     * 营运状态 - 营运
     */
    public static final int OPERATING_STATE_WORK = 0;

    /**
     * 车辆类型
     */
    public static final String VEHICLE_TYPE_DEFAULT = "default";

    /**
     * 识别码
     */
    public static final String CODE_NUM_DEFAULT = "90";

    /**
     * 车辆子类型
     */
    public static final String SUB_TYPE_DEFAULT = "其它车辆";

    /**
     * 车辆类别
     */
    public static final String VEHICLE_CATEGORY_DEFAULT = "default";

    /**
     * 车辆类别名称DEFAULT
     */
    public static final String VEHICLE_CATEGORY_NAME_DEFAULT = "其他车辆";

    /**
     * 分组最大数量
     */
    public static final int ASSIGNMENT_MAX_COUNT = 100;

    /**
     * 默认燃油类型
     */
    public static final String DEFAULT_FUEL_TYPE = "36f0d2ea-375d-4321-9786-ba37b62fd5b1";
}

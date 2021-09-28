package com.zw.platform.domain.enmu;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/25 10:18
 */
public enum VehicleTypeEnum {
    /**
     * 客车
     */
    BUS("10", "客车", 3),
    /**
     * 大型客车
     */
    LARGE_BUS("11", "大型客车", 3),
    /**
     * 中型客车
     */
    MIDDLE_SIZED_BUS("12", "中型客车", 3),
    /**
     * 小型客车
     */
    SMALL_SIZED_BUS("13", "小型客车", 3),
    /**
     * 轿车
     */
    CAR("14", "轿车", 3),
    /**
     * 大型卧铺客车
     */
    LARGE_SLEEPER_BUS("15", "大型卧铺客车", 3),
    /**
     * 中型卧铺客车
     */
    MIDDLE_SIZED_SLEEPER_BUS("16", "中型卧铺客车", 3),
    /**
     * 普通货车
     */
    TRUCK("20", "普通货车", 1),
    /**
     * 大型普通货车
     */
    LARGE_TRUCK("21", "大型普通货车", 1),
    /**
     * 中型普通货车
     */
    MIDDLE_SIZED_TRUCK("22", "中型普通货车", 1),
    /**
     * 小型普通货车
     */
    SMALL_SIZED_TRUCK("23", "小型普通货车", 1),
    /**
     * 专用运输车
     */
    SPECIAL_TRANSPORT_VEHICLE("30", "专用运输车", 1),
    /**
     * 集装箱车
     */
    CONTAINER_CAR("31", "集装箱车", 1),
    /**
     * 大件运输车
     */
    LARGE_TRANSPORT_VEHICLE("32", "大件运输车", 1),
    /**
     * 保温冷藏车
     */
    HEAT_PRESERVATION_REFRIGERATOR_CAR("33", "保温冷藏车", 1),
    /**
     * 商品车运输专用车辆
     */
    COMMERCIAL_VEHICLE_TRANSPORTATION("34", "商品车运输专用车辆", 1),
    /**
     * 罐车
     */
    TANK_CAR("35", "罐车", 1),
    /**
     * 牵引车
     */
    TRUCK_TRACTOR("36", "牵引车", 1),
    /**
     * 挂车
     */
    TRAILER("37", "挂车", 1),
    /**
     * 平板车
     */
    FLAT_CAR("38", "平板车", 1),
    /**
     * 其他专用车
     */
    OTHER_SPECIAL_PURPOSE_VEHICLE("39", "其他专用车", 1),
    /**
     * 危险品运输车
     */
    DANGEROUS_GOODS_TRANSPORT_VEHICLE("40", "危险品运输车", 1),
    /**
     * 农用车
     */
    AGRICULTURAL_VEHICLE("50", "农用车", 1),
    /**
     * 拖拉机
     */
    TRACTOR("60", "拖拉机", 1),
    /**
     * 轮式拖拉机
     */
    WHEELED_TRACTOR("61", "轮式拖拉机", 1),
    /**
     * 手扶拖拉机
     */
    WALKING_TRACTOR("62", "手扶拖拉机", 1),
    /**
     * 履带拖拉机
     */
    CRAWLER_TRACTOR("63", "履带拖拉机", 1),
    /**
     * 特种拖拉机
     */
    SPECIAL_TRACTOR("64", "特种拖拉机", 1),
    /**
     * 其他车辆
     */
    OTHER_VEHICLE("90", "其他车辆", 0);

    public static final Map<String, Integer> CODE_NUM_AND_809_STANDARD = new HashMap<>(16);

    // 初始化基础数据
    static {
        VehicleTypeEnum[] values = VehicleTypeEnum.values();
        for (VehicleTypeEnum value : values) {
            CODE_NUM_AND_809_STANDARD.put(value.getCodeNum(), value.getStandard809());
        }
    }

    /**
     * 编码
     */
    private final String codeNum;

    /**
     * 名称
     */
    private final String name;

    /**
     * 809类别标准 0:通用; 1:货运; 2:工程机械; 3:客运;
     */
    private final Integer standard809;

    VehicleTypeEnum(String codeNum, String name, Integer standard809) {
        this.codeNum = codeNum;
        this.name = name;
        this.standard809 = standard809;
    }

    public String getCodeNum() {
        return codeNum;
    }

    public String getName() {
        return name;
    }

    public Integer getStandard809() {
        return standard809;
    }

    public static Integer get809Standard(String codeNum) {
        if (StringUtils.isBlank(codeNum)) {
            return 0;
        }
        return CODE_NUM_AND_809_STANDARD.getOrDefault(codeNum, 0);
    }
}

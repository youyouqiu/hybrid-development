package com.zw.platform.domain.basicinfo.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/***
 @Author zhengjc
 @Date 2019/5/29 17:14
 @Description 车辆颜色枚举
 @version 1.0
 **/
public enum VehicleColor {

    /**
     * 车牌颜色
     */
    BLACK("黑色", "0"), WHITE("白色", "1"), RED("红色", "2"), BLUE("蓝色", "3"), PURPLE("紫色", "4"), YELLOW("黄色", "5"), GREEN(
        "绿色", "6"), PINK("粉色", "7"), BROWN("棕色", "8"), GRAY("灰色", "9");

    private String code;

    private String name;

    VehicleColor(String name, String code) {
        this.name = name;
        this.code = code;
    }

    private static final Map<String, VehicleColor> colorNameMap = new HashMap<>();

    private static final Map<String, VehicleColor> colorCodeMap = new HashMap<>();

    private static final List<String> vehicleColorNames = new ArrayList<>();

    static {
        for (VehicleColor vc : VehicleColor.values()) {
            colorNameMap.put(vc.name, vc);
            colorCodeMap.put(vc.code, vc);
            vehicleColorNames.add(vc.name);
        }

    }

    /**
     * 根据code获取车辆颜色
     *
     * @param code
     * @return
     */
    private static VehicleColor getColorByCode(String code) {
        return colorCodeMap.get(code);
    }

    /**
     * 根据名称获取车辆颜色
     *
     * @param name
     * @return
     */
    private static VehicleColor getColorByName(String name) {
        return colorNameMap.get(name);
    }

    /**
     * 根据code获取车辆颜色，如果取不到默认黑色
     *
     * @param code
     * @return
     */
    private static String getNameOrDefaultByCode(String code) {
        VehicleColor vehicleColor = Optional.ofNullable(getColorByCode(code)).orElse(BLACK);
        return vehicleColor.name;
    }

    /**
     * 根据名称获取code，如果取不到默认黑色
     *
     * @param name
     * @return
     */
    public static String getCodeOrDefaultByName(String name) {
        VehicleColor vehicleColor = Optional.ofNullable(getColorByName(name)).orElse(BLACK);
        return vehicleColor.code;
    }

    /**
     * 通过code获取颜色名称,没有则返回空字符串
     *
     * @param code
     * @return
     */
    public static String getNameOrBlankByCode(String code) {
        String name = "";
        VehicleColor vehicleColor = colorCodeMap.get(code);
        if (vehicleColor != null) {
            name = vehicleColor.name;
        }
        return name;
    }

    public static String[] getVehicleNames() {
        return vehicleColorNames.toArray(new String[] {});
    }

    public String getNameVal() {
        return name;
    }

    public String getCodeVal() {
        return code;
    }

}

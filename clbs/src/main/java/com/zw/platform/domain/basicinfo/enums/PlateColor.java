package com.zw.platform.domain.basicinfo.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/***
 @Author zhengjc
 @Date 2019/5/29 17:14
 @Description 车牌颜色枚举
 @version 1.0
 **/
public enum PlateColor {
    /**
     * 车牌颜色
     */
    BLUE("蓝色", 1), YELLOW("黄色", 2), BLACK("黑色", 3), WHITE("白色", 4), GREEN("绿色", 5), OTHER("其他", 9), GRADIENT_GREEN(
        "渐变绿色", 94), YELLOW_GREEN("黄绿色", 93), GOLDENROD("农黄", 91), STRONG_GREEN("农绿", 92), DARK_BLUE("农蓝", 90);

    private final Integer code;

    private final String name;

    PlateColor(String name, int code) {
        this.name = name;
        this.code = code;
    }

    private static final Map<String, PlateColor> COLOR_NAME_MAP = new HashMap<>();

    private static final Map<Integer, PlateColor> COLOR_CODE_MAP = new HashMap<>();

    private static final List<String> PLATE_COLOR_NAMES = new ArrayList<>();

    static {
        for (PlateColor pc : PlateColor.values()) {
            COLOR_NAME_MAP.put(pc.name, pc);
            COLOR_CODE_MAP.put(pc.code, pc);
            PLATE_COLOR_NAMES.add(pc.getNameVal());
        }

    }

    /**
     * 通过code获取车牌颜色枚举
     * @param code
     * @return
     */
    private static PlateColor getColorByCode(Integer code) {
        return COLOR_CODE_MAP.get(code);
    }

    /**
     * 通过车牌名称获取车牌颜色枚举
     * @param name
     * @return
     */
    private static PlateColor getColorByName(String name) {
        return COLOR_NAME_MAP.get(name);
    }

    /**
     * 通过code获取车牌颜色枚举
     * @param code
     * @return
     */
    private static String getNameOrDefaultByCode(Integer code) {
        PlateColor plateColor = Optional.ofNullable(getColorByCode(code)).orElse(YELLOW);
        return plateColor.name;
    }

    /**
     * 通过车牌名称获取车牌颜色枚举
     * @param name
     * @return
     */
    public static Integer getCodeOrDefaultByName(String name) {
        PlateColor plateColor = Optional.ofNullable(getColorByName(name)).orElse(YELLOW);
        return plateColor.code;
    }

    /**
     * 通过车牌名称获取车牌颜色枚举
     * @param name
     * @return
     */
    public static Integer getCodeByName(String name) {
        PlateColor plateColor = getColorByName(name);
        if (plateColor == null) {
            return null;
        }
        return plateColor.code;
    }

    /**
     * 通过车牌code获取车牌名称,没有则返回空字符串
     * @param code
     * @return
     */
    public static String getNameOrBlankByCode(Integer code) {
        String name = "";
        PlateColor plateColor = COLOR_CODE_MAP.get(code);
        if (plateColor != null) {
            name = plateColor.name;
        }
        return name;
    }

    /**
     * 通过车牌code获取车牌名称,没有则返回空字符串
     * @param code
     * @return
     */
    public static String getNameOrBlankByCode(String code) {
        Integer codeVal = null;
        try {
            codeVal = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            codeVal = null;
        }
        return getNameOrBlankByCode(codeVal);
    }

    public String getNameVal() {
        return name;
    }

    public Integer getCodeVal() {
        return code;
    }

    public static String[] getPalteColorNames() {
        return PLATE_COLOR_NAMES.toArray(new String[] {});
    }

}
